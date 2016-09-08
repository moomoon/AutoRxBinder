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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import rx.functions.Action;
import rx.functions.Action0;
import rx.functions.ActionN;

/**
 * Created by ants on 9/6/16.
 */

public class RxActionBuilder extends RxBindingBuilder {
    private static final int MAX_PARAMETERIZED_ACTION = 9;
    private static final String METHOD_NAME = "call";

    public RxActionBuilder(String name, RxBindTarget target) {
        super(name, target);
    }

    @Override public TypeName superInterface() {
        return actionInterfaceName(filterNotPartial(getTarget().getMethod().getParameters()));
    }

    @Override protected MethodSpec delegationMethod(Map<Element, FieldSpec> fields, Context context) {
        if (getTarget().getMethod().getParameters().size() > MAX_PARAMETERIZED_ACTION) {
            return rxActionNDelegationMethod(fields, getTarget(), context);
        } else {
            return rxActionDelegationMethod(fields, getTarget(), context);
        }
    }

    private static MethodSpec rxActionDelegationMethod(Map<Element, FieldSpec> fields, RxBindTarget target, Context context) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        return addParametersAndDelegationCall(builder, fields, target, context).build();
    }

    private static MethodSpec rxActionNDelegationMethod(Map<Element, FieldSpec> fields, RxBindTarget target, Context context) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        return addObjectNParametersAndDelegationCall(builder, fields, target, context).build();
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
            typeNames[i] = TypeName.get(parameters.get(i).asType()).box();
        }
        return ParameterizedTypeName.get(ClassName.get(Action.class.getPackage().getName(), "Action" + parameters.size()), typeNames);
    }

}
