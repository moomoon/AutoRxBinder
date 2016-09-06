package com.dxm.rxbinder.rx;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.dxm.rxbinder.Elements.defaultVariableName;
import static com.dxm.rxbinder.rx.Functions.rxDelegationMethod;

/**
 * Created by ants on 9/6/16.
 */

public class RxBinderBuilder {
    private final String name;
    private final ExecutableElement bindingMethod;
    private final TypeElement enclosingType;

    public RxBinderBuilder(String name, ExecutableElement bindingMethod, TypeElement enclosingType) {
        this.name = name;
        this.bindingMethod = bindingMethod;
        this.enclosingType = enclosingType;
    }

    protected String funcName() {
        return "call";
    }

    public String getName() {
        return name;
    }

    public ExecutableElement getBindingMethod() {
        return bindingMethod;
    }

    public TypeElement getEnclosingType() {
        return enclosingType;
    }

    public TypeSpec.Builder asTypeSpec() {
        FieldSpec delegationField = FieldSpec.builder(TypeName.get(enclosingType.asType()), defaultVariableName(enclosingType), Modifier.PRIVATE, Modifier.FINAL).build();
        ParameterSpec constructorParameter = ParameterSpec.builder(delegationField.type, delegationField.name).build();
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addField(delegationField)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(constructorParameter)
                        .addStatement("this.$N = $N", delegationField, constructorParameter).build())
                .addMethod(rxDelegationMethod(delegationField, bindingMethod));
    }

//    private MethodSpec delegate(FieldSpec self, ExecutableElement method) {
//
//        return MethodSpec.methodBuilder(funcName()).returns(TypeName.get(method.getReturnType()))
//    }
}
