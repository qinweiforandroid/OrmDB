package com.qinwei.ormdb.sample.db;

import android.database.sqlite.SQLiteDatabase;
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
        throw new IllegalArgumentException("your class[" + clazz.getSimpleName() + "] fields must have one id=true Column Annotation");
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
        throw new IllegalArgumentException("your class[" + t.getClass().getSimpleName() + "] fields must have one id=true Column Annotation");
    }

    public static void createTable(SQLiteDatabase database, Class<?> clazz) {
        DBLog.d("createTable class name " + clazz.getSimpleName());
        if (clazz.isAnnotationPresent(Table.class)) {
            StringBuilder sql = new StringBuilder();
            String tableName = getTableName(clazz);
            sql.append("create table if not exists " + tableName + "( ");
            Field[] fields = clazz.getDeclaredFields();
            Field field = null;
            for (int i = 0; i < fields.length; i++) {
                field = fields[i];
                field.setAccessible(true);
                if (field.isAnnotationPresent(Column.class)) {
                    String columnName = getColumnName(field);
                    if (field.getAnnotation(Column.class).id()) {
                        sql.append(columnName + " TEXT PRIMARY KEY");
                    } else {
                        Class<?> type = field.getType();
                        if (type == String.class) {
                            sql.append(columnName + " TEXT ");
                        } else if (type == int.class || type == Integer.class) {
                            sql.append(columnName + " INTEGER ");
                        } else {
                            // FIXME: 2017/2/25 other impl
                        }
                    }
                    sql.append(",");
                }
            }
            sql.delete((sql.length() - 1), sql.length());
            sql.append(")");
            DBLog.d("createTable sql=" + sql.toString());
            database.execSQL(sql.toString());
        } else {
            throw new IllegalArgumentException("you class[" + clazz.getSimpleName() + "] must add Table annotation ");
        }
    }

    private static boolean isUseFieldTypeDefault(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.DEFAULT;
    }

    private static boolean isUseFieldTypeVarchar(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.VARCHAR;
    }

    private static boolean isUseFieldTypeText(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.TEXT;
    }

    private static boolean isUseFieldTypeInteger(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.INTEGER;
    }

    private static boolean isUseFieldTypeSerializable(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.SERIALIZABLE;
    }

    private static boolean isUseFieldTypeTone(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.TONE;
    }

    private static boolean isUseFieldTypeTmany(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.TMANY;
    }

    private static boolean isUseFieldTypeBlob(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.BLOB;
    }
}
