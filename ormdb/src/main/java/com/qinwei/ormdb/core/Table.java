package com.qinwei.ormdb.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by qinwei on 2017/2/25.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name() default "";
}
