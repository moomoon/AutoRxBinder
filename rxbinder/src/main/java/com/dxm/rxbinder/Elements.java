package com.dxm.rxbinder;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import javafx.util.Pair;

/**
 * Created by ants on 9/6/16.
 */
public class Elements {

    public static TypeSpec.Builder findOrCreateBinderBuilder(Map<TypeMirror, Pair<String, TypeSpec.Builder>> map, TypeElement type) {
        TypeMirror mirror = type.asType();
        Pair<String, TypeSpec.Builder> packageAndBuilder = map.get(mirror);
        if (null == packageAndBuilder) {
            String packageName = findPackage(type).getQualifiedName().toString();
            TypeSpec.Builder builder = TypeSpec.classBuilder(binderEnclosingClassNameFor(type.getSimpleName().toString())).addModifiers(Modifier.FINAL, Modifier.PUBLIC);
            packageAndBuilder = new Pair<>(packageName, builder);
            map.put(mirror, packageAndBuilder);
        }
        return packageAndBuilder.getValue();
    }

    private static String binderEnclosingClassNameFor(String className) {
        return "Rx" + className + "Bindings";
    }

    public static TypeElement findTopLevelType(ExecutableElement methodElement) {
        Element topLevel = null;
        Element element = methodElement;
        while (null != (element = findEnclosingType(element))) {
            topLevel = element;
        }
        return (TypeElement) topLevel;
    }

    public static TypeElement findEnclosingType(Element element) {
        Element elem = element;
        for (; ; ) {
            elem = elem.getEnclosingElement();
            if (null == elem) return null;
            switch (elem.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                    return (TypeElement) elem;
            }
        }
    }

    public static PackageElement findPackage(Element element) {
        Element elem = element;
        for (; ; ) {
            if (elem.getKind() == ElementKind.PACKAGE) {
                return (PackageElement) elem;
            } else {
                elem = elem.getEnclosingElement();
            }
        }
    }

    public static String binderClassNameFor(String name) {
        return name;
    }

    public static String defaultVariableName(TypeElement element) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, element.getSimpleName().toString());
    }


}
