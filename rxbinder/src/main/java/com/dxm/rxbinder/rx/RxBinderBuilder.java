package com.dxm.rxbinder.rx;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.annotations.Partial;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

import static com.dxm.rxbinder.Utils.deduplicateName;
import static com.dxm.rxbinder.Utils.defaultVariableName;
import static com.dxm.rxbinder.Utils.findEnclosingType;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

/**
 * Created by ants on 9/6/16.
 */

public abstract class RxBinderBuilder {
    private final String name;
    private final ExecutableElement bindingMethod;
    private final Context context;

    public RxBinderBuilder(String name, ExecutableElement bindingMethod, Context context) {
        this.name = name;
        this.bindingMethod = bindingMethod;
        this.context = context;
    }

    public String getName() {
        return name;
    }

    public ExecutableElement getBindingMethod() {
        return bindingMethod;
    }

    public Context getContext() {
        return context;
    }

    public TypeSpec.Builder typeSpecBuilder() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC);
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
        LinkedHashMap<Element, FieldSpec> fields = createFields(getBindingMethod());
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
                .addMethod(delegationMethod(fields))
                .addSuperinterface(superInterface());
    }

    protected abstract MethodSpec delegationMethod(Map<Element, FieldSpec> fields);

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
        Set<String> fieldNames = newHashSetWithExpectedSize(fields.size());
        if (!method.getModifiers().contains(Modifier.STATIC)) {
            String fieldName = defaultVariableName(enclosingType);
            fields.put(enclosingType, FieldSpec.builder(TypeName.get(enclosingType.asType()), fieldName, Modifier.PRIVATE, Modifier.FINAL).build());
            fieldNames.add(fieldName);
        }
        for (VariableElement parameter : partialParameters) {
            String fieldName = deduplicateName(fieldNames, parameter.getSimpleName().toString());
            fieldNames.add(fieldName);
            fields.put(parameter, FieldSpec.builder(TypeName.get(parameter.asType()), fieldName, Modifier.PRIVATE, Modifier.FINAL).build());
        }
        return fields;
    }

    static MethodSpec.Builder addParametersAndDelegationCall(MethodSpec.Builder builder, Map<Element, FieldSpec> fields, ExecutableElement delegatedMethod) {
        final int numParams = delegatedMethod.getParameters().size();
        final Object receiver;
        StringBuilder sb = new StringBuilder();
        if (delegatedMethod.getReturnType().getKind() != TypeKind.VOID) sb.append("return ");
        if (delegatedMethod.getModifiers().contains(Modifier.STATIC)) {
            sb.append("$T.");
            receiver = ClassName.get(findEnclosingType(delegatedMethod));
        } else {
            sb.append("$N.");
            receiver = fields.get(findEnclosingType(delegatedMethod));
        }
        sb.append(delegatedMethod.getSimpleName()).append('(');
        for (int i = 0; i < numParams; i++) {
            if (i > 0) sb.append(", ");
            VariableElement param = delegatedMethod.getParameters().get(i);
            FieldSpec field = fields.get(param);
            if (null == field) {
                builder.addParameter(ParameterSpec.builder(TypeName.get(param.asType()), param.getSimpleName().toString()).build());
                sb.append(param.getSimpleName().toString());
            } else {
                sb.append("this.").append(field.name);
            }
        }
        sb.append(')');
        return builder.addStatement(sb.toString(), receiver);
    }

    static MethodSpec.Builder addObjectNParametersAndDelegationCall(MethodSpec.Builder builder, Map<Element, FieldSpec> fields, ExecutableElement delegatedMethod) {
        final int numParams = delegatedMethod.getParameters().size();
        ParameterSpec param = ParameterSpec.builder(ArrayTypeName.of(TypeName.OBJECT), "args").build();
        StringBuilder sb = new StringBuilder();
        if (delegatedMethod.getReturnType().getKind() != TypeKind.VOID) sb.append("return ");
        List<Object> obj = new ArrayList<>();
        if (delegatedMethod.getModifiers().contains(Modifier.STATIC)) {
            sb.append("$T.");
            obj.add(ClassName.get(findEnclosingType(delegatedMethod)));
        } else {
            sb.append("$N.");
            obj.add(fields.get(findEnclosingType(delegatedMethod)));
        }
        sb.append(delegatedMethod.getSimpleName()).append('(');
        for (int i = 0; i < numParams; i++) {
            if (i > 0) sb.append(", ");
            VariableElement methodParam = delegatedMethod.getParameters().get(i);
            FieldSpec field = fields.get(methodParam);
            if (null == field) {
                sb.append("($T)$N[").append(i).append(']');
                obj.add(delegatedMethod.getParameters().get(i).asType());
                obj.add(param);
            } else {
                sb.append("this.$N");
                obj.add(field);
            }
        }
        sb.append(')');

        return builder.varargs(true)
                .addParameter(param)
                .addStatement(sb.toString(), obj.toArray());
    }

    static List<VariableElement> filterNotPartial(List<? extends VariableElement> parameters) {
        List<VariableElement> l = new ArrayList<>();
        for (VariableElement parameter: parameters) {
            if (null == parameter.getAnnotation(Partial.class)) {
                l.add(parameter);
            }
        }
        return l;
    }

}
