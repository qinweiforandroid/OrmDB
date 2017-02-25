package com.qinwei.ormdb.sample.db;

import android.text.TextUtils;

import java.lang.reflect.Field;

/**
 * Created by qinwei on 2017/2/25.
 */

public class DBUtil {
    public static String getTableName(Class<?> clazz) {
        String name = clazz.getAnnotation(Table.class).name();
        return TextUtils.isEmpty(name) ? clazz.getSimpleName() : name;
    }

    public static String getColumnName(Field f) {
        String columnName = f.getAnnotation(Column.class).name();
        return TextUtils.isEmpty(columnName) ? f.getName() : columnName;
    }
}
