package com.dxm.rxbinder.rx;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.TryBlock;
import com.dxm.rxbinder.annotations.Partial;
import com.dxm.rxbinder.annotations.RxBind;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.TypeKindVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.dxm.rxbinder.Utils.deduplicateName;
import static com.dxm.rxbinder.Utils.defaultVariableName;
import static com.dxm.rxbinder.Utils.findEnclosingType;
import static com.dxm.rxbinder.Utils.readTypeNameFromName;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

/**
 * Created by ants on 9/6/16.
 */

public abstract class RxBindingBuilder {
    private final RxBindTarget target;
    private final String name;
    private static final String RUNTIME_EXCEPTION_NAME = "java.lang.RuntimeException";
    private static final String EXCEPTION_NAME = "java.lang.Exception";
    private static final String OBJECT_NAME = "java.lang.Object";

    public RxBindingBuilder(String name, RxBindTarget target) {
        this.target = target;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RxBindTarget getTarget() {
        return target;
    }

    public TypeSpec.Builder typeSpecBuilder(Context context) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC);
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
        LinkedHashMap<Element, FieldSpec> fields = createFields(getTarget().getMethod());
        for (FieldSpec field : fields.values()) {
            classBuilder.addField(field);
            ParameterSpec parameter = ParameterSpec.builder(field.type, field.name).build();
            constructorBuilder.addParameter(parameter).addStatement("this.$N = $N", field, parameter);
        }
        MethodSpec constructor = constructorBuilder.build();
        if (constructor.parameters.size() > 0) {
            classBuilder.addMethod(constructor);
        }
        return classBuilder
                .addMethod(delegationMethod(fields, context))
                .addSuperinterface(superInterface());
    }

    protected abstract MethodSpec delegationMethod(Map<Element, FieldSpec> fields, Context context);

    public abstract TypeName superInterface();

    private static LinkedHashMap<Element, FieldSpec> createFields(ExecutableElement method) {
        final TypeElement enclosingType = findEnclosingType(method);
        List<VariableElement> partialParameters = newArrayList();
        for (VariableElement parameter : method.getParameters()) {
            if (null != parameter.getAnnotation(Partial.class)) {
                partialParameters.add(parameter);
            }
        }
        LinkedHashMap<Element, FieldSpec> fields = new LinkedHashMap<>();
        Set<String> names = newHashSetWithExpectedSize(fields.size());
        if (!method.getModifiers().contains(Modifier.STATIC)) {
            String fieldName = defaultVariableName(enclosingType);
            fields.put(enclosingType, FieldSpec.builder(TypeName.get(enclosingType.asType()), fieldName, Modifier.PRIVATE, Modifier.FINAL).build());
            names.add(fieldName);
        }
        for (VariableElement parameter : partialParameters) {
            String fieldName = deduplicateName(names, parameter.getSimpleName().toString());
            names.add(fieldName);
            fields.put(parameter, FieldSpec.builder(TypeName.get(parameter.asType()), fieldName, Modifier.PRIVATE, Modifier.FINAL).build());
        }
        return fields;
    }

    private static TryBlock tryBlock(RxBindTarget target, Context context) {
        final ClassName exception = readTypeNameFromName(target.getBind().exception());
        if (null == exception) {
            throw new RuntimeException("" + target.getBind().exception() + " is not a valid class name. Use the class name returned from Foo.class.getName().");
        }
        final TryBlock.Builder blockBuilder = TryBlock.builder();
        for (TypeMirror throwableType : target.getMethod().getThrownTypes()) {
            if (!shouldBeCaught(throwableType, context)) continue;;
            blockBuilder.addCatch((TypeElement) context.getProcessingEnvironment().getTypeUtils().asElement(throwableType), exception);
        }
        return blockBuilder.build();
    }

    private static boolean shouldBeCaught(TypeMirror type, Context context) {
        final Types types = context.getProcessingEnvironment().getTypeUtils();
        TypeElement throwable = (TypeElement) types.asElement(type);
        switch (throwable.getQualifiedName().toString()) {
            case RUNTIME_EXCEPTION_NAME:
            case OBJECT_NAME:
                return false;
            case EXCEPTION_NAME:
                return true;
        }
        for (TypeMirror superType : types.directSupertypes(type)) {
            TypeElement superElement = (TypeElement) types.asElement(superType);
            if (superElement.getKind() != ElementKind.CLASS) continue;
            return shouldBeCaught(superType, context);
        }
        return false;
    }


    static MethodSpec.Builder addParametersAndDelegationCall(MethodSpec.Builder builder, Map<Element, FieldSpec> fields, RxBindTarget target, Context context) {
        final ExecutableElement method = target.getMethod();
        final int numParams = method.getParameters().size();
        final Object receiver;
        TryBlock tryBlock = tryBlock(target, context);
        StringBuilder sb = new StringBuilder();
        Set<String> names = new HashSet<>();
        if (method.getReturnType().getKind() != TypeKind.VOID) sb.append("return ");
        if (method.getModifiers().contains(Modifier.STATIC)) {
            sb.append("$T.");
            ClassName className = ClassName.get(findEnclosingType(method));
            receiver = className;
            names.add(className.simpleName());
        } else {
            sb.append("this.$N.");
            receiver = fields.get(findEnclosingType(method));
        }
        sb.append(method.getSimpleName()).append('(');
        for (int i = 0; i < numParams; i++) {
            if (i > 0) sb.append(", ");
            VariableElement param = method.getParameters().get(i);
            FieldSpec field = fields.get(param);
            if (null == field) {
                String name = deduplicateName(names, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, param.getSimpleName().toString()));
                names.add(name);
                builder.addParameter(ParameterSpec.builder(TypeName.get(param.asType()).box(), name).build());
                sb.append(name);
            } else {
                sb.append("this.").append(field.name);
            }
        }
        sb.append(')');
        tryBlock.start(builder);
        builder.addStatement(sb.toString(), receiver);
        tryBlock.end(builder, names, context);

        return builder;
    }


    static MethodSpec.Builder addObjectNParametersAndDelegationCall(MethodSpec.Builder builder, Map<Element, FieldSpec> fields, RxBindTarget target, Context context) {
        final ExecutableElement method = target.getMethod();
        final int numParams = method.getParameters().size();
        ParameterSpec param = ParameterSpec.builder(ArrayTypeName.of(TypeName.OBJECT), "args").build();
        StringBuilder sb = new StringBuilder();
        Set<String> names = new HashSet<>();
        TryBlock tryBlock = tryBlock(target, context);
        if (method.getReturnType().getKind() != TypeKind.VOID) sb.append("return ");
        List<Object> obj = new ArrayList<>();
        if (method.getModifiers().contains(Modifier.STATIC)) {
            sb.append("$T.");
            ClassName className = ClassName.get(findEnclosingType(method));
            obj.add(className);
            names.add(className.simpleName());
        } else {
            sb.append("this.$N.");
            obj.add(fields.get(findEnclosingType(method)));
        }
        sb.append(method.getSimpleName()).append('(');
        for (int i = 0; i < numParams; i++) {
            if (i > 0) sb.append(", ");
            VariableElement methodParam = method.getParameters().get(i);
            FieldSpec field = fields.get(methodParam);
            if (null == field) {
                sb.append("($T)$N[").append(i).append(']');
                obj.add(method.getParameters().get(i).asType());
                obj.add(param);
            } else {
                sb.append("this.$N");
                obj.add(field);
            }
        }
        sb.append(')');

        builder.varargs(true).addParameter(param);
        tryBlock.start(builder);
        builder.addStatement(sb.toString(), obj.toArray());
        tryBlock.end(builder, names, context);
        return builder;
    }

    static List<VariableElement> filterNotPartial(List<? extends VariableElement> parameters) {
        List<VariableElement> l = new ArrayList<>();
        for (VariableElement parameter : parameters) {
            if (null == parameter.getAnnotation(Partial.class)) {
                l.add(parameter);
            }
        }
        return l;
    }

}
