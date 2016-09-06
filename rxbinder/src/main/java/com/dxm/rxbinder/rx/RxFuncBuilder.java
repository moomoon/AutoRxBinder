package com.dxm.rxbinder.rx;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static com.dxm.rxbinder.rx.Functions.funcInterfaceName;

/**
 * Created by ants on 9/6/16.
 */

public class RxFuncBuilder extends RxBinderBuilder {
    public RxFuncBuilder(String name, ExecutableElement bindingMethod, TypeElement enclosingType) {
        super(name, bindingMethod, enclosingType);
    }

    @Override
    public TypeSpec.Builder asTypeSpec() {
        return super.asTypeSpec().addSuperinterface(funcInterfaceName(getBindingMethod().getParameters(), getBindingMethod().getReturnType()));
    }
}
