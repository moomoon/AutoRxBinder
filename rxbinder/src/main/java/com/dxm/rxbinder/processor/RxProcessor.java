package com.dxm.rxbinder.processor;

import com.dxm.rxbinder.Context;
import com.dxm.rxbinder.Pair;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.TypeMirror;



/**
 * Created by ants on 9/6/16.
 */
public interface RxProcessor {
    void process(Map<TypeMirror, Pair<String, TypeSpec.Builder>> builders, Context context);

    List<RxProcessor> ALL = new ArrayList<RxProcessor>() {{
        add(new RxBindProcessor());
    }};
}
