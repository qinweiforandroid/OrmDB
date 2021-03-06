package com.qinwei.ormdb.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.qinwei.ormdb.BaseDao;
import com.qinwei.ormdb.CacheDaoManager;
import com.qinwei.ormdb.DBException;
import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;
import com.qinwei.ormdb.log.DBLog;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */

public class DBUtil {
    public static String getTableName(Class<?> clazz) {
        String name = clazz.getAnnotation(Table.class).name();
        return TextUtils.isEmpty(name) ? clazz.getSimpleName() : name;
    }

    public static String getColumnName(Field f) {
        return getColumnName(f, f.getAnnotation(Column.class));
    }

    public static String getColumnName(Field f, Column column) {
        String columnName = column.name();
        return TextUtils.isEmpty(columnName) ? f.getName() : columnName;
    }

    public static String getIdColumnName(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(Column.class)) {
                if (fields[i].getAnnotation(Column.class).id()) {
                    return getColumnName(fields[i]);
                }
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().getName().equals(Object.class.getName())) {
            return getIdColumnName(clazz.getSuperclass());
        }
        throw new IllegalArgumentException("your class[" + clazz.getSimpleName() + "] fields must have one id=true Column Annotation");
    }

    public static String getIdFieldName(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(Column.class)) {
                if (fields[i].getAnnotation(Column.class).id()) {
                    return fields[i].getName();
                }
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().getName().equals(Object.class.getName())) {
            return getIdColumnName(clazz.getSuperclass());
        }
        throw new IllegalArgumentException("your class[" + clazz.getSimpleName() + "] fields must have one id=true Column Annotation");

    }

    public static <T> String getIdValue(T t) throws IllegalAccessException {
        return getIdValue(t, t.getClass(), getIdFieldName(t.getClass()));
    }

    public static <T> String getIdValue(T t, String mIdFieldName) throws IllegalAccessException {
        return getIdValue(t, t.getClass(), mIdFieldName);
    }

    public static <T> String getIdValue(T t, Class<?> clazz, String mIdFieldName) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (TextUtils.equals(fields[i].getName(), mIdFieldName)) {
                return fields[i].get(t).toString();
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().getName().equals(Object.class.getName())) {
            return getIdValue(t, clazz.getSuperclass(), getIdFieldName(clazz.getSuperclass()));
        }
        throw new IllegalArgumentException("your class[" + t.getClass().getSimpleName() + "] fields must have one id=true Column Annotation");
    }

    public static void createTable(SQLiteDatabase database, Class<?> clazz) {
        DBLog.d("create table class name " + clazz.getSimpleName());
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
                    sql.append(getOneCreateTableStmt(field));
                }
            }
            sql.delete((sql.length() - 1), sql.length());
            sql.append(")");
            DBLog.d("create table sql:" + sql.toString());
            database.execSQL(sql.toString());
        } else {
            throw new IllegalArgumentException("you class[" + clazz.getSimpleName() + "] must add Table annotation ");
        }
    }

    public static String getOneCreateTableStmt(Field field) {
        String columnName = getColumnName(field);
        String columnType = "";
        if (field.getAnnotation(Column.class).id()) {
            columnType = "TEXT PRIMARY KEY";
        } else {
            Class<?> clazz = field.getType();
            if (clazz == String.class) {
                columnType = "TEXT";
            } else if (clazz == int.class || clazz == Integer.class || clazz == long.class) {
                columnType = "INTEGER";
            } else {
                // FIXME: 2017/2/25 other impl
                Column.ColumnType type = field.getAnnotation(Column.class).type();
                if (type == Column.ColumnType.UNKNOWN) {
                    throw new IllegalArgumentException("you must add columnType for special object");
                }
                switch (type) {
                    case SERIALIZABLE:
                        columnType = "BLOB";
                        break;
                    case TONE:
                    case BIGDECIMAL:
                        columnType = "TEXT";
                        break;
                    case INTEGER:
                        columnType = "INTEGER";
                        break;
                    case VARCHAR:
                        columnType = "VARCHAR";
                        break;
                    default:
                        break;
                }
            }
        }
        return columnName + " " + columnType + ",";
    }


    public static boolean isUseFieldTypeDefault(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.UNKNOWN;
    }

    public static boolean isUseFieldTypeVarchar(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.VARCHAR;
    }

    public static boolean isUseFieldTypeText(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.TEXT;
    }

    public static boolean isUseFieldTypeInteger(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.INTEGER;
    }

    public static boolean isUseFieldTypeSerializable(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.SERIALIZABLE;
    }

    public static boolean isUseFieldTypeTone(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.TONE;
    }

    public static boolean isUseFieldTypeTMany(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.TMANY;
    }

    public static boolean isUseFieldTypeBlob(Field field) {
        return field.getAnnotation(Column.class).type() == Column.ColumnType.BLOB;
    }


    public static void execSQL(SQLiteDatabase mDatabase, String sql) {
        mDatabase.execSQL(sql);
    }

    public static void execSQL(SQLiteDatabase mDatabase, String sql, Object[] bindArgs) {
        mDatabase.execSQL(sql, bindArgs);
    }

    public static <T> BaseDao<T> getDao(SQLiteDatabase mDatabase, Class<T> clz) {
        return CacheDaoManager.getInstance().get(mDatabase, clz);
    }

    public static <T> long newOrUpdate(SQLiteDatabase mDatabase, T t) throws DBException {
        return getDao(mDatabase, t.getClass()).newOrUpdate(t);
    }

    public static <T> long delete(SQLiteDatabase mDatabase, Class<T> clazz, String where, String[] whereArgs) {
        return getDao(mDatabase, clazz).delete(where, whereArgs);
    }

    public static <T> long delete(SQLiteDatabase mDatabase, Class<T> clazz, String id) {
        return getDao(mDatabase, clazz).delete(id);
    }

    public static <T> ArrayList<T> queryAll(SQLiteDatabase mDatabase, Class<T> clazz) throws DBException {
        return getDao(mDatabase, clazz).queryAll();
    }

    public static <T> Cursor rawQuery(SQLiteDatabase mDatabase, Class<T> clazz, String sql, String[] selectionArgs) {
        return getDao(mDatabase, clazz).rawQuery(sql, selectionArgs);
    }

    public static ArrayList<String> queryArrayString(SQLiteDatabase mDatabase, Class clazz, String sql, String[] selectionArgs) {
        ArrayList<String> values = new ArrayList<>();
        Cursor cursor = rawQuery(mDatabase, clazz, sql, selectionArgs);
        while (cursor.moveToNext()) {
            values.add(cursor.getString(0));
        }
        cursor.close();
        return values;
    }

    public static String queryString(SQLiteDatabase mDatabase, Class clazz, String sql, String[] selectionArgs) {
        ArrayList<String> values = queryArrayString(mDatabase, clazz, sql, selectionArgs);
        return values.size() > 0 ? values.get(0) : "";
    }

    public static <T> ArrayList<T> query(SQLiteDatabase mDatabase, String sql, Class<T> clazz) {
        ArrayList<T> ts = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery(sql, null);
        try {
            T t;
            Field f;
            while (cursor.moveToNext()) {
                t = clazz.newInstance();
                Field[] fields = t.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    f = fields[i];
                    f.setAccessible(true);
                    if (f.isAnnotationPresent(Column.class)) {
                        dto(t, f, cursor);
                    }
                }
                ts.add(t);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return ts;
    }

    public static <T> void dto(T t, Field f, Cursor cursor) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        Type type = f.getType();
        String columnName = DBUtil.getColumnName(f);
        if (type == String.class) {
            f.set(t, cursor.getString(cursor.getColumnIndex(columnName)));
        } else if (type == int.class || type == Integer.class) {
            f.set(t, cursor.getInt(cursor.getColumnIndex(columnName)));
        } else {
            Column column = f.getAnnotation(Column.class);
            Column.ColumnType columnType = column.type();
            if (columnType == Column.ColumnType.UNKNOWN) {
                throw new IllegalArgumentException("you must add columnType for special object");
            }
            if (columnType == Column.ColumnType.SERIALIZABLE) {
                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(columnName));
                if (bytes != null) {
                    f.set(t, SerializableUtil.toObject(bytes));
                } else {
                    f.set(t, null);
                }
            } else if (columnType == Column.ColumnType.INTEGER) {
                f.set(t, cursor.getInt(cursor.getColumnIndex(columnName)));
            } else if (columnType == Column.ColumnType.BIGDECIMAL) {
                f.set(t, new BigDecimal(cursor.getDouble(cursor.getColumnIndex(columnName))));
            } else if (columnType == Column.ColumnType.VARCHAR) {
                f.set(t, cursor.getString(cursor.getColumnIndex(columnName)));
            }
        }
    }

    public static String toString(Object o) {
        return o == null ? "" : o.toString();
    }
}
