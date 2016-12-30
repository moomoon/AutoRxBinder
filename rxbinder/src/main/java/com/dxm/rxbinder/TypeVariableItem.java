package com.dxm.rxbinder;

import com.google.auto.value.AutoValue;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

/**
 * Created by ants on 30/12/2016.
 */

public abstract class TypeVariableItem {
    abstract Element getElement();

    abstract TypeVariable getVariable();
    @AutoValue
    public abstract static class MethodBound extends TypeVariableItem {
        abstract ExecutableElement getElement();

        public static MethodBound from(ExecutableElement element, TypeVariable typeVariable) {
            return new AutoValue_TypeVariableItem_MethodBound(typeVariable, element);
        }
    }

    @AutoValue
    public abstract static class TypeBound extends TypeVariableItem {
        abstract TypeElement getElement();

        public static TypeBound from(TypeElement element, TypeVariable typeVariable) {
            return new AutoValue_TypeVariableItem_TypeBound(typeVariable, element);
        }
    }

}
