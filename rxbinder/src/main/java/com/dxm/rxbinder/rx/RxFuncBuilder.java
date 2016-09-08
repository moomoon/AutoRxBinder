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
import javax.lang.model.type.TypeMirror;

import rx.functions.FuncN;
import rx.functions.Function;

/**
 * Created by ants on 9/6/16.
 */

public class RxFuncBuilder extends RxBindingBuilder {
    private static final int MAX_PARAMETERIZED_FUNC = 9;
    private static final String METHOD_NAME = "call";

    public RxFuncBuilder(String name, RxBindTarget target) {
        super(name, target);
    }

    @Override public TypeName superInterface() {
        final ExecutableElement method = getTarget().getMethod();
        return funcInterfaceName(filterNotPartial(method.getParameters()), method.getReturnType());
    }

    @Override protected MethodSpec delegationMethod(Map<Element, FieldSpec> fields, Context context) {
        if (getTarget().getMethod().getParameters().size() > MAX_PARAMETERIZED_FUNC) {
            return rxFuncNDelegationMethod(fields, getTarget(), context);
        } else {
            return rxFuncDelegationMethod(fields, getTarget(), context);
        }
    }

    private static MethodSpec rxFuncDelegationMethod(Map<Element, FieldSpec> fields, RxBindTarget target, Context context) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(target.getMethod().getReturnType()));
        return addParametersAndDelegationCall(builder, fields, target, context).build();
    }

    private static MethodSpec rxFuncNDelegationMethod(Map<Element, FieldSpec> fields, RxBindTarget target, Context context) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(target.getMethod().getReturnType()));
        return addObjectNParametersAndDelegationCall(builder, fields, target, context).build();
    }

    private static TypeName funcInterfaceName(List<? extends VariableElement> parameters, TypeMirror returnType) {
        final int numParams = parameters.size();
        if (numParams > MAX_PARAMETERIZED_FUNC) {
            return ParameterizedTypeName.get(ClassName.get(FuncN.class), TypeName.get(returnType));
        }
        TypeName[] typeNames = new TypeName[parameters.size() + 1];
        for (int i = 0; i < parameters.size(); i++) {
            typeNames[i] = TypeName.get(parameters.get(i).asType()).box();
        }
        typeNames[parameters.size()] = TypeName.get(returnType).box();
        return ParameterizedTypeName.get(ClassName.get(Function.class.getPackage().getName(), "Func" + parameters.size()), typeNames);
    }


}
