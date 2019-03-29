package com.qinwei.ormdb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;
import com.qinwei.ormdb.log.DBLog;
import com.qinwei.ormdb.util.DBUtil;
import com.qinwei.ormdb.util.SerializableUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;

public class DBManager {
    private static DBManager mInstance;
    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase mDatabase;

    private Context context;

    private DBManager(Context context, SQLiteOpenHelper helper) {
        this.context = context;
        mHelper = helper;
        mDatabase = mHelper.getWritableDatabase();

    }

    public static void init(Context context, SQLiteOpenHelper helper) {
        if (mInstance == null) {
            mInstance = new DBManager(context, helper);
        }
    }

    public static DBManager getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("you must call init method to init db manager");
        }
        return mInstance;
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        mDatabase.endTransaction();
    }

    public void execSQL(String sql) {
        mDatabase.execSQL(sql);
    }

    public void execSQL(String sql, Object[] bindArgs) {
        mDatabase.execSQL(sql, bindArgs);
    }

    public void release() {
        mDatabase.close();
        mInstance = null;
    }

    public <T> BaseDao<T> getDao(Class<T> clz) {
        return CacheDaoManager.getInstance().get(mDatabase, clz);
    }

    public <T> long newOrUpdate(T t) throws DBException {
        return getDao(t.getClass()).newOrUpdate(t);
    }

    public <T> long delete(Class<T> clazz, String where, String[] whereArgs) {
        return getDao(clazz).delete(where, whereArgs);
    }

    public <T> long delete(Class<T> clazz, String id) {
        return getDao(clazz).delete(id);
    }

    public <T> ArrayList<T> queryAll(Class<T> clazz) throws DBException {
        return getDao(clazz).queryAll();
    }

    public <T> Cursor rawQuery(Class<T> clazz, String sql, String[] selectionArgs) {
        return getDao(clazz).rawQuery(sql, selectionArgs);
    }

    public ArrayList<String> queryArrayString(Class clazz, String sql, String[] selectionArgs) {
        ArrayList<String> values = new ArrayList<>();
        Cursor cursor = rawQuery(clazz, sql, selectionArgs);
        while (cursor.moveToNext()) {
            values.add(cursor.getString(0));
        }
        cursor.close();
        return values;
    }

    public String queryString(Class clazz, String sql, String[] selectionArgs) {
        ArrayList<String> values = queryArrayString(clazz, sql, selectionArgs);
        return values.size() > 0 ? values.get(0) : "";
    }

    public <T> ArrayList<T> query(String sql, Class<T> clazz) {
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

    public <T> void dto(T t, Field f, Cursor cursor) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
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

}
