package com.qinwei.ormdb.sample.db;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by qinwei on 2017/2/25.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    enum ColumnType {
        DEFAULT,
        VARCHAR,
        TEXT,
        BLOB,
        INTEGER,
        DOUBLE,
        SERIALIZABLE,
        TONE,
        TMANY
    }

    String name() default "";

    boolean id() default false;

    ColumnType type() default ColumnType.DEFAULT;

}
