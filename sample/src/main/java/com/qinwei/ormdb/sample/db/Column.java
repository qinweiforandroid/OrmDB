package com.qinwei.ormdb.sample.db;

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
        SERIALIZABLE,
        TONE,
        TMANY
    }

    String name() default "";

    boolean id() default false;

    ColumnType type() default ColumnType.UNKNOWN;

}
