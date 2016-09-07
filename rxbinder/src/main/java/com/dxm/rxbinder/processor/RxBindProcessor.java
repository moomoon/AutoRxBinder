package com.dxm.rxbinder.processor;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.Utils;
import com.dxm.rxbinder.annotations.Partial;
import com.dxm.rxbinder.annotations.RxBind;
import com.dxm.rxbinder.rx.RxActionBuilder;
import com.dxm.rxbinder.rx.RxBinderBuilder;
import com.dxm.rxbinder.rx.RxFuncBuilder;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashSet;
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

import static com.dxm.rxbinder.Utils.deduplicateMethodName;
import static com.dxm.rxbinder.Utils.deduplicateName;
import static com.dxm.rxbinder.Utils.deduplicateNestedClassName;
import static com.dxm.rxbinder.Utils.defaultVariableName;
import static com.dxm.rxbinder.Utils.findEnclosingType;
import static com.dxm.rxbinder.Utils.findOrCreateBinderBuilder;
import static com.dxm.rxbinder.Utils.findTopLevelType;
import javafx.util.Pair;

/**
 * Created by ants on 9/6/16.
 */
public class RxBindProcessor implements RxProcessor {

    @Override
    public void process(Map<TypeMirror, Pair<String, TypeSpec.Builder>> builders, Context context) {
        for (Element element: context.getRoundEnvironment().getElementsAnnotatedWith(RxBind.class)) {
            if (element.getKind() != ElementKind.METHOD) return;
            RxBind bind = element.getAnnotation(RxBind.class);
            final ExecutableElement methodElement = (ExecutableElement) element;
            final TypeElement type = findTopLevelType(methodElement);
            final Pair<String, TypeSpec.Builder> packageAndTypeBuilder = findOrCreateBinderBuilder(builders, type);
            final Pair<MethodSpec, TypeSpec> binding = createBinding(bind, methodElement, packageAndTypeBuilder.getValue().build(), context);
            packageAndTypeBuilder.getValue().addType(binding.getValue());
            packageAndTypeBuilder.getValue().addMethod(binding.getKey());
        }
    }

    private static Pair<MethodSpec, TypeSpec> createBinding(RxBind bind, ExecutableElement bindingMethod, TypeSpec containerClass, Context context) {
        final String name = bind.name();
        String binderClassName = name.isEmpty() ? binderClassNameFor(bind, bindingMethod) : Utils.binderClassNameFor(name);
        String binderMethodName = name.isEmpty() ? binderMethodNameFor(bind, bindingMethod) : Utils.binderMethodNameFor(name);
        binderClassName = deduplicateNestedClassName(containerClass, binderClassName);
        binderMethodName = deduplicateMethodName(containerClass, binderMethodName);
        Pair<TypeSpec, TypeName> binderClassAndSuperInterface = buildBinderClass(binderClassName, bindingMethod, context);
        MethodSpec method = binderMethod(binderMethodName, bindingMethod, binderClassAndSuperInterface.getKey(), binderClassAndSuperInterface.getValue());
        return new Pair<>(method, binderClassAndSuperInterface.getKey());
    }

    private static Pair<TypeSpec, TypeName> buildBinderClass(String name, ExecutableElement bindingMethod, Context context) {
        final RxBinderBuilder builder;
        if (bindingMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
            builder = new RxActionBuilder(name, bindingMethod, context);
        } else {
            builder = new RxFuncBuilder(name, bindingMethod, context);
        }
        return new Pair<>(builder.typeSpecBuilder().build(), builder.superInterface());
    }

    private static MethodSpec binderMethod(String name, ExecutableElement bindingMethod, TypeSpec binderClass, TypeName superInterface) {
        StringBuilder sb = new StringBuilder("return new $N(");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(superInterface);
        List<Object> obj = new ArrayList<>();
        obj.add(binderClass);
        Set<String> names = new HashSet<>();
        if (!bindingMethod.getModifiers().contains(Modifier.STATIC)) {
            TypeElement enclosingType = findEnclosingType(bindingMethod);
            String varName = defaultVariableName(enclosingType);
            names.add(varName);
            ParameterSpec param = ParameterSpec.builder(ClassName.get(enclosingType), varName).build();
            builder.addParameter(param);
            sb.append("$N");
            obj.add(param);
        }
        for (VariableElement methodParam : bindingMethod.getParameters()) {
            if (null == methodParam.getAnnotation(Partial.class)) continue;
            if (obj.size() > 1) {
                sb.append(", ");
            }
            final String varName = deduplicateName(names, methodParam.getSimpleName().toString());
            names.add(varName);
            ParameterSpec param = ParameterSpec.builder(ClassName.get(methodParam.asType()), varName).build();
            builder.addParameter(param);
            sb.append("$N");
            obj.add(param);
        }
        sb.append(')');
        return builder.addStatement(sb.toString(), obj.toArray()).build();
    }

    private static String binderClassNameFor(RxBind bind, ExecutableElement method) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, method.getSimpleName().toString()) + "Binder";
    }

    private static String binderMethodNameFor(RxBind bind, ExecutableElement method) {
        return method.getSimpleName().toString();
    }

}
