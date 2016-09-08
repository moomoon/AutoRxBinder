package com.dxm.rxbinder.processor;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.Utils;
import com.dxm.rxbinder.annotations.Partial;
import com.dxm.rxbinder.annotations.RxBind;
import com.dxm.rxbinder.rx.RxActionBuilder;
import com.dxm.rxbinder.rx.RxBindTarget;
import com.dxm.rxbinder.rx.RxBindingBuilder;
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
import static com.dxm.rxbinder.Utils.findOrCreateBindingBuilder;
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
            final RxBindTarget target = new RxBindTarget(bind, methodElement);
            final TypeElement type = findTopLevelType(methodElement);
            final Pair<String, TypeSpec.Builder> packageAndTypeBuilder = findOrCreateBindingBuilder(builders, type);
            final Pair<MethodSpec, TypeSpec> binding = createBinding(packageAndTypeBuilder.getValue().build(), target, context);
            packageAndTypeBuilder.getValue().addType(binding.getValue());
            packageAndTypeBuilder.getValue().addMethod(binding.getKey());
        }
    }

    private static Pair<MethodSpec, TypeSpec> createBinding(TypeSpec containerClass, RxBindTarget target, Context context) {
        final String name = target.getBind().name();
        String binderClassName = name.isEmpty() ? bindingClassNameFor(target) : Utils.bindingClassNameFor(name);
        String binderMethodName = name.isEmpty() ? bindingMethodNameFor(target) : Utils.bindingMethodNameFor(name);
        binderClassName = deduplicateNestedClassName(containerClass, binderClassName);
        binderMethodName = deduplicateMethodName(containerClass, binderMethodName);
        Pair<TypeSpec, TypeName> binderClassAndSuperInterface = buildBindingClass(binderClassName, target, context);
        MethodSpec method = bindingMethod(binderMethodName, binderClassAndSuperInterface.getKey(), binderClassAndSuperInterface.getValue(), target);
        return new Pair<>(method, binderClassAndSuperInterface.getKey());
    }

    private static Pair<TypeSpec, TypeName> buildBindingClass(String name, RxBindTarget target, Context context) {
        final RxBindingBuilder builder;
        if (target.getMethod().getReturnType().getKind().equals(TypeKind.VOID)) {
            builder = new RxActionBuilder(name, target);
        } else {
            builder = new RxFuncBuilder(name, target);
        }
        return new Pair<>(builder.typeSpecBuilder(context).build(), builder.superInterface());
    }

    private static MethodSpec bindingMethod(String name, TypeSpec bindingClass, TypeName superInterface, RxBindTarget target) {
        StringBuilder sb = new StringBuilder("return new $N(");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(superInterface);
        List<Object> obj = new ArrayList<>();
        obj.add(bindingClass);
        Set<String> names = new HashSet<>();
        if (!target.getMethod().getModifiers().contains(Modifier.STATIC)) {
            TypeElement enclosingType = findEnclosingType(target.getMethod());
            String varName = defaultVariableName(enclosingType);
            names.add(varName);
            ParameterSpec param = ParameterSpec.builder(ClassName.get(enclosingType), varName).build();
            builder.addParameter(param);
            sb.append("$N");
            obj.add(param);
        }
        for (VariableElement methodParam : target.getMethod().getParameters()) {
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

    private static String bindingClassNameFor(RxBindTarget target) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, target.getMethod().getSimpleName().toString()) + "Binder";
    }

    private static String bindingMethodNameFor(RxBindTarget target) {
        return target.getMethod().getSimpleName().toString();
    }

}
