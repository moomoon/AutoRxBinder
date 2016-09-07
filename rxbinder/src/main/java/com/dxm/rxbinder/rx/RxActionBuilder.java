package com.dxm.rxbinder.rx;

import com.dxm.rxbinder.Context;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import rx.functions.Action;
import rx.functions.Action0;
import rx.functions.ActionN;

/**
 * Created by ants on 9/6/16.
 */

public class RxActionBuilder extends RxBinderBuilder {
    private static final int MAX_PARAMETERIZED_ACTION = 9;
    private static final String METHOD_NAME = "call";

    public RxActionBuilder(String name, ExecutableElement bindingMethod, Context context) {
        super(name, bindingMethod, context);
    }

    @Override public TypeName superInterface() {
        return actionInterfaceName(filterNotPartial(getBindingMethod().getParameters()));
    }

    @Override protected MethodSpec delegationMethod(Map<Element, FieldSpec> fields) {
        if (getBindingMethod().getParameters().size() > MAX_PARAMETERIZED_ACTION) {
            return rxActionNDelegationMethod(fields, getBindingMethod());
        } else {
            return rxActionDelegationMethod(fields, getBindingMethod());
        }
    }

    private static MethodSpec rxActionDelegationMethod(Map<Element, FieldSpec> fields, ExecutableElement delegatedMethod) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        return addParametersAndDelegationCall(builder, fields, delegatedMethod).build();
    }

    private static MethodSpec rxActionNDelegationMethod(Map<Element, FieldSpec> fields, ExecutableElement delegatedMethod) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        return addObjectNParametersAndDelegationCall(builder, fields, delegatedMethod).build();
    }

    private static TypeName actionInterfaceName(List<? extends VariableElement> parameters) {
        final int numParams = parameters.size();
        if (numParams > MAX_PARAMETERIZED_ACTION) {
            return ClassName.get(ActionN.class);
        }
        if (numParams == 0) {
            return ClassName.get(Action0.class);
        }
        TypeName[] typeNames = new TypeName[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            typeNames[i] = TypeName.get(parameters.get(i).asType());
        }
        return ParameterizedTypeName.get(ClassName.get(Action.class.getPackage().getName(), "Action" + parameters.size()), typeNames);
    }

}
