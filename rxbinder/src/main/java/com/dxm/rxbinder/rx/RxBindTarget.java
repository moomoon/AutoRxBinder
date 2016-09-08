package com.dxm.rxbinder.rx;

import com.dxm.rxbinder.annotations.RxBind;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by ants on 9/8/16.
 */

public class RxBindTarget {
    private final RxBind bind;
    private final ExecutableElement method;

    public RxBindTarget(RxBind bind, ExecutableElement method) {
        this.bind = bind;
        this.method = method;
    }

    public RxBind getBind() {
        return bind;
    }

    public ExecutableElement getMethod() {
        return method;
    }
}
