package com.dxm.rxbinder;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by ants on 30/12/2016.
 */

public enum TypeLevel {
    Static, Enclosed, Local, Anonymous;

    public static TypeLevel get(TypeElement element) {
        switch (element.getNestingKind()) {
            case TOP_LEVEL:
                return Static;
            case MEMBER:
                return element.getModifiers().contains(Modifier.STATIC) ? Static : Enclosed;
            case LOCAL:
                return Local;
            case ANONYMOUS:
            default:
                return Anonymous;

        }
    }
}
