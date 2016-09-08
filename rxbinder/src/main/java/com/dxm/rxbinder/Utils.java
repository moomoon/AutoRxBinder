package com.dxm.rxbinder;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import javafx.util.Pair;

/**
 * Created by ants on 9/7/16.
 */

public class Utils {
    public static String deduplicateMethodName(TypeSpec type, String methodName) {
        Set<String> names = new HashSet<>(type.methodSpecs.size());
        for (MethodSpec method : type.methodSpecs) {
            names.add(method.name);
        }
        return deduplicateName(names, methodName);
    }

    public static String deduplicateNestedClassName(TypeSpec type, String className) {
        Set<String> names = new HashSet<>(type.methodSpecs.size());
        for (TypeSpec nestedClass : type.typeSpecs) {
            names.add(nestedClass.name);
        }
        return deduplicateName(names, className);
    }

    public static String deduplicateName(Set<String> existingNames, String name) {
        for (int i = 0; ; i++) {
            String dedup = i == 0 ? name : name + i;
            if (!existingNames.contains(dedup)) return dedup;
        }
    }

    public static Pair<String, TypeSpec.Builder> findOrCreateBindingBuilder(Map<TypeMirror, Pair<String, TypeSpec.Builder>> map, TypeElement type) {
        TypeMirror mirror = type.asType();
        Pair<String, TypeSpec.Builder> packageAndBuilder = map.get(mirror);
        if (null == packageAndBuilder) {
            String packageName = findPackage(type).getQualifiedName().toString();
            TypeSpec.Builder builder = TypeSpec.classBuilder(bindingContainerClassNameFor(type.getSimpleName().toString())).addModifiers(Modifier.FINAL, Modifier.PUBLIC);
            packageAndBuilder = new Pair<>(packageName, builder);
            map.put(mirror, packageAndBuilder);
        }
        return packageAndBuilder;
    }

    private static String bindingContainerClassNameFor(String className) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, className) + "Bindings";
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

    public static String bindingClassNameFor(String name) {
        return LOWER_CAMEL.to(UPPER_CAMEL, name) + "Binding";
    }

    public static String bindingMethodNameFor(String name) {
        return name;
    }

    public static String defaultVariableName(TypeElement element) {
        return UPPER_CAMEL.to(LOWER_CAMEL, element.getSimpleName().toString());
    }

    public static ClassName readTypeNameFromName(String name) {
        String[] classes = name.split("\\$");
        int packageDelimeter = classes[0].lastIndexOf('.');
        if (packageDelimeter < 0) {
            return null;
        }
        String packageName = classes[0].substring(0, packageDelimeter);
        String simpleName = classes[0].substring(packageDelimeter + 1, classes[0].length());
        if (classes.length == 1) {
            return ClassName.get(packageName, simpleName);
        }
        String[] simpleNames = new String[classes.length - 1];
        System.arraycopy(classes, 1, simpleNames, 0, simpleName.length());
        return ClassName.get(packageName, simpleName, simpleNames);
    }
}
