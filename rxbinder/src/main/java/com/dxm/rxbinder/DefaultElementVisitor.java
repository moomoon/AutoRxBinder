package com.dxm.rxbinder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor7;

/**
 * Created by ants on 30/12/2016.
 */

public abstract class DefaultElementVisitor<R, P> extends AbstractElementVisitor7<R, P> {
    @Override public R visitPackage(PackageElement e, P p) {
        return visitUnknown(e, p);
    }

    @Override public R visitType(TypeElement e, P p) {
        return visitUnknown(e, p);
    }

    @Override public R visitVariable(VariableElement e, P p) {
        return visitUnknown(e, p);
    }

    @Override public R visitExecutable(ExecutableElement e, P p) {
        return visitUnknown(e, p);
    }

    @Override public R visitTypeParameter(TypeParameterElement e, P p) {
        return visitUnknown(e, p);
    }
}
