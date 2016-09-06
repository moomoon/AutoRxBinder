package com.dxm.rxbinder;

import com.dxm.rxbinder.processor.RxProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import javafx.util.Pair;

/**
 * Created by ants on 9/5/16.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.dxm.rxbinder.*")
public class RxBinderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Map<TypeMirror, Pair<String, TypeSpec.Builder>> builders = new LinkedHashMap<TypeMirror, Pair<String, TypeSpec.Builder>>();
        final Context context = new Context(processingEnv, roundEnv);
        for (RxProcessor p: RxProcessor.ALL) {
            p.process(builders, context);
        }
        for (Pair<String, TypeSpec.Builder> packageAndType : builders.values()) {
            try {
                JavaFile.builder(packageAndType.getKey(), packageAndType.getValue().build()).build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
