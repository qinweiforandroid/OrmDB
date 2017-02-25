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

    public static String getColumnId(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(Column.class)) {
                if (fields[i].getAnnotation(Column.class).id()) {
                    return getColumnName(fields[i]);
                }
            }
        }
        throw new IllegalArgumentException("your class fields must have one id=true Column Annotation");
    }

    public static <T> String getIdValue(T t) throws IllegalAccessException {
        Field[] fields = t.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(Column.class)) {
                if (fields[i].getAnnotation(Column.class).id()) {
                    return fields[i].get(t).toString();
                }
            }
        }
        throw new IllegalArgumentException("your class fields must have one id=true Column Annotation");
    }
}
