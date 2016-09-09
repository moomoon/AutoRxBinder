package com.dxm.rxbinder;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * Created by ants on 9/8/16.
 */

public class TryBlock {
    private final List<CatchBlock> catchBlocks;

    private TryBlock(TryBlock.Builder builder) {
        this.catchBlocks = ImmutableList.copyOf(builder.catchBlocks);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start(MethodSpec.Builder builder) {
        if (catchBlocks.size() > 0) {
            builder.beginControlFlow("try");
        }
    }

    public void end(MethodSpec.Builder builder, Set<String> usedNames, Context context) {
        if (catchBlocks.size() == 0) return;
        for (CatchBlock catchBlock : catchBlocks) {
            catchBlock.addTo(builder, usedNames, context);
        }
        builder.endControlFlow();
    }

    public  static class CatchBlock {
        private final TypeElement thrownException;
        private final TypeName wrapper;

        private CatchBlock(TypeElement thrownException, TypeName wrapper) {
            this.thrownException = thrownException;
            this.wrapper = wrapper;
        }

        private void addTo(MethodSpec.Builder builder, Set<String> usedNames, Context context) {
            String name = Utils.deduplicateName(usedNames, Utils.defaultVariableName(thrownException));
            usedNames.add(name);
            builder.nextControlFlow("catch ($T " + name + ")", thrownException);
            builder.addStatement("throw new $T(" + name + ")", wrapper);
        }
    }

    public static class Builder {
        private List<CatchBlock> catchBlocks = new ArrayList<>();

        public Builder addCatch(TypeElement throwException, TypeName wrapper) {
            catchBlocks.add(new CatchBlock(throwException, wrapper));
            return this;
        }

        public TryBlock build() {
            return new TryBlock(this);
        }
    }

}
