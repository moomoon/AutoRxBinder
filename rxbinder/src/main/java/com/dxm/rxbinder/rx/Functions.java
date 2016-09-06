package com.dxm.rxbinder.rx;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import rx.functions.ActionN;
import rx.functions.FuncN;

/**
 * Created by ants on 9/6/16.
 */

public class Functions {
    private static final String FUNCTIONS_PKG = "rx.functions";
    private static final String FUNC_METHOD_NAME = "call";
    private static final String ACTION_METHOD_NAME = "call";
    public static final int MAX_PARAMETERIZED_FUNC = 9;
    public static final int MAX_PARAMETERIZED_ACTION = 9;


    public static TypeName funcInterfaceName(List<? extends VariableElement> parameters, TypeMirror returnType) {
        final int numParams = parameters.size();
        if (numParams > MAX_PARAMETERIZED_FUNC) {
            return ParameterizedTypeName.get(ClassName.get(FuncN.class), TypeName.get(returnType));
        }
        TypeName[] typeNames = new TypeName[parameters.size() + 1];
        for (int i = 0; i < parameters.size(); i++) {
            typeNames[i] = TypeName.get(parameters.get(i).asType());
        }
        typeNames[parameters.size()] = TypeName.get(returnType);
        return ParameterizedTypeName.get(ClassName.get(FUNCTIONS_PKG, "Func" + parameters.size()), typeNames);
    }

    public static TypeName actionInterfaceName(List<? extends VariableElement> parameters) {
        final int numParams = parameters.size();
        if (numParams > MAX_PARAMETERIZED_ACTION) {
            return ClassName.get(ActionN.class);
        }
        TypeName[] typeNames = new TypeName[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            typeNames[i] = TypeName.get(parameters.get(i).asType());
        }
        return ParameterizedTypeName.get(ClassName.get(FUNCTIONS_PKG, "Action" + parameters.size()), typeNames);
    }

    public static MethodSpec rxDelegationMethod(FieldSpec delegatedField, ExecutableElement delegatedMethod) {
        if (delegatedMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
            return rxActionDelegationMethod(delegatedField, delegatedMethod);
        }
        return rxFuncDelegationMethod(delegatedField, delegatedMethod);
    }

    private static MethodSpec rxFuncDelegationMethod(FieldSpec delegatedField, ExecutableElement delegatedMethod) {
        final int numParams = delegatedMethod.getParameters().size();
        if (numParams > MAX_PARAMETERIZED_FUNC) {
            return rxFuncNDelegationMethod(delegatedField, delegatedMethod);
        }
        MethodSpec.Builder builder = MethodSpec.methodBuilder(FUNC_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(delegatedMethod.getReturnType()));
        return addParametersAndDelegationCall(builder, delegatedField, delegatedMethod, true).build();
    }

    private static MethodSpec rxFuncNDelegationMethod(FieldSpec delegatedField, ExecutableElement delegatedMethod) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(FUNC_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(delegatedMethod.getReturnType()));
        return addObjectNParametersAndDelegationCall(builder, delegatedField, delegatedMethod, true).build();
    }

    private static MethodSpec.Builder addObjectNParametersAndDelegationCall(MethodSpec.Builder builder, FieldSpec delegatedField, ExecutableElement delegatedMethod, boolean shouldReturn) {
        final int numParams = delegatedMethod.getParameters().size();
        ParameterSpec param = ParameterSpec.builder(ArrayTypeName.of(TypeName.OBJECT), "args").build();
        StringBuilder sb = new StringBuilder();
        if (shouldReturn) sb.append("return ");
        sb.append("$N.").append(delegatedMethod.getSimpleName()).append('(');
        Object[] params = new Object[numParams * 2 + 1];
        params[0] = delegatedField;
        for (int i = 0; i < numParams; i++) {
            if (i > 0) sb.append(", ");
            sb.append("($T)$N[").append(i).append(']');
            params[i * 2 + 1] = delegatedMethod.getParameters().get(i).asType();
            params[i * 2 + 2] = param;
        }
        return builder.varargs(true)
                .addParameter(param)
                .addStatement(sb.toString(), params);
    }

    private static MethodSpec.Builder addParametersAndDelegationCall(MethodSpec.Builder builder, FieldSpec delegatedField, ExecutableElement delegatedMethod, boolean shouldReturn) {
        final int numParams = delegatedMethod.getParameters().size();
        StringBuilder sb = new StringBuilder();
        if(shouldReturn) sb.append("return ");
        sb.append("$N.").append(delegatedMethod.getSimpleName()).append('(');
        for (int i = 0; i < numParams; i++) {
            VariableElement param = delegatedMethod.getParameters().get(i);
            builder.addParameter(ParameterSpec.builder(TypeName.get(param.asType()), param.getSimpleName().toString()).build());
            if (i > 0) sb.append(", ");
            sb.append(param.getSimpleName().toString());
        }
        sb.append(')');
        builder.addStatement(sb.toString(), delegatedField);
        return builder;
    }

    private static MethodSpec rxActionDelegationMethod(FieldSpec delegatedField, ExecutableElement delegatedMethod) {
        final int numParams = delegatedMethod.getParameters().size();
        if (numParams > MAX_PARAMETERIZED_FUNC) {
            return rxFuncNDelegationMethod(delegatedField, delegatedMethod);
        }
        MethodSpec.Builder builder = MethodSpec.methodBuilder(ACTION_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        return addParametersAndDelegationCall(builder, delegatedField, delegatedMethod, false).build();

    }

    private static MethodSpec rxActionNDelegationMethod(FieldSpec delegatedField, ExecutableElement delegatedMethod) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(ACTION_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        return addObjectNParametersAndDelegationCall(builder, delegatedField, delegatedMethod, false).build();
    }

}
