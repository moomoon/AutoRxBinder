package com.dxm.rxbinder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ants on 9/5/16.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface RxBind {
    String name() default "";
}
