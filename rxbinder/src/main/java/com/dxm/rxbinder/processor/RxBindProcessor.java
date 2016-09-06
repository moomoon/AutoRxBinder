package com.dxm.rxbinder.processor;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.Elements;
import com.dxm.rxbinder.RxBind;
import com.dxm.rxbinder.rx.Functions;
import com.dxm.rxbinder.rx.RxActionBuilder;
import com.dxm.rxbinder.rx.RxFuncBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.dxm.rxbinder.Elements.defaultVariableName;
import static com.dxm.rxbinder.Elements.findEnclosingType;
import static com.dxm.rxbinder.Elements.findOrCreateBinderBuilder;
import static com.dxm.rxbinder.Elements.findTopLevelType;
import static com.dxm.rxbinder.rx.Functions.actionInterfaceName;
import static com.dxm.rxbinder.rx.Functions.funcInterfaceName;
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
            final TypeElement enclosingType = findEnclosingType(methodElement);
            final TypeSpec.Builder binderTypeBuilder = findOrCreateBinderBuilder(builders, type);
            final Pair<MethodSpec, TypeSpec> binding = createBinding(bind, methodElement, enclosingType);
            binderTypeBuilder.addType(binding.getValue());
            binderTypeBuilder.addMethod(binding.getKey());
        }
    }

    private static Pair<MethodSpec, TypeSpec> createBinding(RxBind bind, ExecutableElement bindingMethod, TypeElement enclosingType) {
        final String name = bind.name();
        String binderClassName = name.isEmpty() ? binderClassNameFor(bind, bindingMethod) : Elements.binderClassNameFor(name);
        TypeSpec binderClass = buildBinderClass(binderClassName, bindingMethod, enclosingType);
        MethodSpec method = MethodSpec.methodBuilder(binderClassName).returns(TypeName.BOOLEAN).addStatement("return true").build();
        return new Pair<>(method, binderClass);
    }

    private static TypeSpec buildBinderClass(String name, ExecutableElement bindingMethod, TypeElement enclosingType) {
        if (bindingMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
            return new RxActionBuilder(name, bindingMethod, enclosingType).asTypeSpec().build();
        }
        return new RxFuncBuilder(name, bindingMethod, enclosingType).asTypeSpec().build();
    }

    public static String binderClassNameFor(RxBind bind, ExecutableElement method) {
        return method.getSimpleName().toString() + "Binder";
    }
}
