package com.qinwei.ormdb.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by qinwei on 2017/2/25.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    enum ColumnType {
        UNKNOWN,
        VARCHAR,
        TEXT,
        BLOB,
        INTEGER,
        BIGDECIMAL,
        SERIALIZABLE,
        TONE,
        TMANY
    }


    String name() default "";

    boolean id() default false;

    ColumnType type() default ColumnType.UNKNOWN;

    boolean autorefresh() default false;

}