package com.dxm.rxbinder.rx;

import com.dxm.rxbinder.DefaultElementVisitor;
import com.dxm.rxbinder.TypeLevel;
import com.dxm.rxbinder.TypeVariableItem;
import com.dxm.rxbinder.Utils;
import com.dxm.rxbinder.annotations.RxBind;
import com.dxm.variable.Val;
import com.dxm.variable.Variables;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.AbstractElementVisitor7;

/**
 * Created by ants on 9/8/16.
 */

public class RxBindTarget {
    private final RxBind bind;
    private final ExecutableElement method;
    private final TypeElement topLevelType;
    //up to the first static type
    private final List<TypeElement> enclosingTypes;
    private final Map<TypeVariableItem, TypeVariableName> uniqueTypeVariables;

    public RxBindTarget(RxBind bind, ExecutableElement method) {
        this.bind = bind;
        this.method = method;
        this.topLevelType = Utils.findTopLevelType(method);
        this.enclosingTypes = getEnclosingTypes(method);
        this.uniqueTypeVariables = getUniqueTypeVariables(method, enclosingTypes);
    }

    private static List<TypeElement> getEnclosingTypes(ExecutableElement method) {
        List<TypeElement> types = new LinkedList<>();
        loop:
        for (TypeElement element = Utils.findEnclosingType(method); ; element = Utils.findEnclosingType(element)) {
            switch (TypeLevel.get(element)) {
                case Static:
                    types.add(element);
                    break loop;
                case Enclosed:
                    types.add(element);
                    break;
                case Local:
                    throw new UnsupportedOperationException("RxBind cannot be annotated to local class members");
                case Anonymous:
                default:
                    throw new UnsupportedOperationException("RxBind cannot be annotated to annonymous class members");
            }
        }
        return Collections.unmodifiableList(types);
    }

    private static Map<TypeVariableItem, TypeVariableName> getUniqueTypeVariables(ExecutableElement method, List<TypeElement> enclosingTypes) {
        Map<TypeVariableItem, TypeVariableName> map = new HashMap<>();
        Set<String> names = new HashSet<>();
        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            String name = Utils.deduplicateName(names, typeParameterElement.getSimpleName().toString());
            names.add(name);
            map.put(TypeVariableItem.MethodBound.from(method, (TypeVariable) typeParameterElement.asType()), getTypeVariableName(name, typeParameterElement.getBounds()));
        }
        for (TypeElement enclosingType : enclosingTypes) {
            for (TypeParameterElement typeParameterElement : enclosingType.getTypeParameters()) {
                String name = Utils.deduplicateName(names, typeParameterElement.getSimpleName().toString());
                names.add(name);
                map.put(TypeVariableItem.TypeBound.from(enclosingType, (TypeVariable) typeParameterElement.asType()), getTypeVariableName(name, typeParameterElement.getBounds()));
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public RxBind getBind() {
        return bind;
    }

    public ExecutableElement getMethod() {
        return method;
    }

    public List<TypeVariableName> getTypeVariableNames(Element element) {
        final List<TypeVariableName> names = new LinkedList<>();
        new DefaultElementVisitor<Void, Void>() {
            @Override public Void visitExecutable(ExecutableElement e, Void aVoid) {
                for (TypeParameterElement typeParameterElement : e.getTypeParameters()) {
                    TypeVariableName name = uniqueTypeVariables.get(TypeVariableItem.MethodBound.from(e, (TypeVariable) typeParameterElement.asType()));
                    if (null == name) {
                        throw new IllegalStateException("Cannot get type variable name for " + e + " <" + typeParameterElement + ">.");
                    }
                    names.add(name);
                }
                return null;
            }

            @Override public Void visitType(TypeElement e, Void aVoid) {
                for (TypeParameterElement typeParameterElement : e.getTypeParameters()) {
                    TypeVariableName name = uniqueTypeVariables.get(TypeVariableItem.TypeBound.from(e, (TypeVariable) typeParameterElement.asType()));
                    if (null == name) {
                        throw new IllegalStateException("Cannot get type variable name for " + e + " <" + typeParameterElement + ">.");
                    }
                    names.add(name);
                }
                return null;
            }

            @Override public Void visitUnknown(Element e, Void aVoid) {
                throw new UnsupportedOperationException("Cannot get type variable names for " + e);
            }
        }.visit(element);
        return Collections.unmodifiableList(names);
    }

    public TypeVariableName getTypeVariableName(Element element, TypeParameterElement typeParameterElement) {
        return typeVariableName.visit(element, typeParameterElement);
    }

    private final DefaultElementVisitor<TypeVariableName, TypeParameterElement> typeVariableName = new DefaultElementVisitor<TypeVariableName, TypeParameterElement>() {
        @Override public TypeVariableName visitType(TypeElement e, TypeParameterElement typeParameterElement) {
            TypeVariableName name = uniqueTypeVariables.get(TypeVariableItem.TypeBound.from(e, (TypeVariable) typeParameterElement.asType()));
            return null == name ? visitUnknown(e, typeParameterElement) : name;
        }

        @Override public TypeVariableName visitExecutable(ExecutableElement e, TypeParameterElement typeParameterElement) {
            TypeVariableName name = uniqueTypeVariables.get(TypeVariableItem.MethodBound.from(e, (TypeVariable) typeParameterElement.asType()));
            return null == name ? visitUnknown(e, typeParameterElement) : name;
        }

        @Override public TypeVariableName visitUnknown(Element e, TypeParameterElement typeParameterElement) {
            throw new UnsupportedOperationException("Cannot get type variable names for " + e);
        }
    };

    private static TypeVariableName getTypeVariableName(String name, List<? extends TypeMirror> bounds) {
        TypeName[] boundNames = new TypeName[bounds.size()];
        for (int i = 0; i < bounds.size(); i++) boundNames[i] = TypeName.get(bounds.get(i));
        return TypeVariableName.get(name, boundNames);
    }
}
