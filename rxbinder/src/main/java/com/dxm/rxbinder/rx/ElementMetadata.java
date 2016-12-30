package com.dxm.rxbinder.rx;

import com.google.auto.value.AutoValue;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor7;

/**
 * Created by ants on 30/12/2016.
 */


@AutoValue
public abstract class ElementMetadata {
    abstract String getType();

    abstract String getName();

    public static Builder builder() {
        return new AutoValue_ElementMetadata.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setType(String type);

        public abstract Builder setName(String name);

        public abstract ElementMetadata build();
    }

    public static ElementMetadata from(Element element) {
        return FromElement.Instance.visit(element);
    }

    private static class FromElement extends AbstractElementVisitor7<ElementMetadata, Void> {
        private FromElement() {}
        private final static FromElement Instance = new FromElement();
        @Override public ElementMetadata visitPackage(PackageElement e, Void aVoid) {
            return builder().setType("Package").setName(e.getQualifiedName().toString()).build();
        }

        @Override public ElementMetadata visitType(TypeElement e, Void aVoid) {
            return builder().setType("Type").setName(e.getQualifiedName().toString()).build();
        }

        @Override public ElementMetadata visitVariable(VariableElement e, Void aVoid) {
            return builder().setType("Variable").setName(e.getSimpleName().toString()).build();
        }

        @Override public ElementMetadata visitExecutable(ExecutableElement e, Void aVoid) {
            return builder().setType("Executable").build();
        }

        @Override public ElementMetadata visitTypeParameter(TypeParameterElement e, Void aVoid) {
            return null;
        }
    }

}
