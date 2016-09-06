package com.dxm.rxbinder.processor;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.Elements;
import com.dxm.rxbinder.RxObservable;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.dxm.rxbinder.Elements.findOrCreateBinderBuilder;
import static com.dxm.rxbinder.Elements.findTopLevelType;
import javafx.util.Pair;

/**
 * Created by ants on 9/6/16.
 */
//public class RxObservableProcessor implements RxProcessor {
//    @Override
//    public void process(Map<TypeMirror, Pair<String, TypeSpec.Builder>> builders, Context context) {
//        for (Element element : context.getRoundEnvironment().getElementsAnnotatedWith(RxObservable.class)) {
//            if (element.getKind() != ElementKind.METHOD) return;
//            Class<?> clazz;
//            RxObservable observable = element.getAnnotation(RxObservable.class);
//            final ExecutableElement methodElement = (ExecutableElement) element;
//            final TypeElement type = findTopLevelType(methodElement);
//            TypeSpec.Builder binderTypeBuilder = findOrCreateBinderBuilder(builders, type);
//            final Pair<MethodSpec, TypeSpec> binding = createBinding(observable, methodElement);
//            binderTypeBuilder.addType(binding.getValue());
//            binderTypeBuilder.addMethod(binding.getKey());
//        }
//
//    }
//
//    private static Pair<MethodSpec, TypeSpec> createBinding(RxObservable observable, ExecutableElement bindingMethod) {
//        final String name = observable.name();
//        String binderClassName = name.isEmpty() ? binderClassNameFor(observable, bindingMethod) : Elements.binderClassNameFor(name);
//        TypeSpec binderClass = TypeSpec.classBuilder(binderClassName).build();
//        return null;
//    }
//
//    public static String binderClassNameFor(RxObservable observable, ExecutableElement method) {
//
//    }
//
//}
