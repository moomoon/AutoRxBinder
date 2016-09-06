package com.dxm.rxbinder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

/**
 * Created by ants on 9/6/16.
 */
public enum BinderMethodType {
    Mapper, Setter, Observable;

    public static BinderMethodType from(RxBind bind, ExecutableElement methodElement) {
        if(methodElement.getReturnType().getKind().equals(TypeKind.VOID)) {
            return BinderMethodType.Setter;
        }
        return BinderMethodType.Mapper;
    }
}
