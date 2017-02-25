package com.qinwei.ormdb.sample.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qinwei.ormdb.sample.domain.Company;
import com.qinwei.ormdb.sample.domain.Developer;
import com.qinwei.ormdb.sample.domain.Skill;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ormDB.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public SQLiteDatabase getDB() {
        return getWritableDatabase();
    }

    /**
     * 新增或者修改表记录
     *
     * @param t   obj will insert or update to db
     * @param <T>
     * @return >0代表执行成功 -1代码执行失败
     */
    public <T> long newOrUpdate(T t) {
        try {
            if (t.getClass().isAnnotationPresent(Table.class)) {
                ContentValues values = new ContentValues();
                Field[] fields = t.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    if (f.isAnnotationPresent(Column.class)) {
                        String columnName = DBUtil.getColumnName(f);
                        if (f.getType() == String.class) {
                            values.put(columnName, f.get(t).toString());
                        } else if (f.getType() == int.class || f.getType() == Integer.class) {
                            values.put(columnName, f.getInt(t));
                        } else {
                            // FIXME: 2017/2/25 other type
                        }
                    }
                }
                return getDB().replace(DBUtil.getTableName(t.getClass()), null, values);
            } else {
                throw new RuntimeException("you class[" + t.getClass().getSimpleName() + "] must add Table annotation ");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 删除一个对象
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> long delete(T t) {
        try {
            if (t.getClass().isAnnotationPresent(Table.class)) {
                return getDB().delete(DBUtil.getTableName(t.getClass()), DBUtil.getColumnId(t.getClass()) + "=?", new String[]{DBUtil.getIdValue(t)});
            } else {
                throw new RuntimeException("you class[" + t.getClass().getSimpleName() + "] must add Table annotation ");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 根据id查询并转化为obj
     *
     * @param id
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T queryById(String id, Class<T> clazz) {
        try {
            T t = null;
            if (clazz.isAnnotationPresent(Table.class)) {
                String sql = "select * from " + DBUtil.getTableName(clazz) + " where " + DBUtil.getColumnId(clazz) + "=?";
                Cursor cursor = getDB().rawQuery(sql, new String[]{id});
                Field f = null;
                while (cursor.moveToNext()) {
                    t = clazz.newInstance();
                    Field[] fields = t.getClass().getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        f = fields[i];
                        f.setAccessible(true);
                        if (f.isAnnotationPresent(Column.class)) {
                            Type type = f.getType();
                            if (type == String.class) {
                                f.set(t, cursor.getString(cursor.getColumnIndex(DBUtil.getColumnName(f))));
                            } else if (type == int.class || type == Integer.class) {
                                f.set(t, cursor.getInt(cursor.getColumnIndex(DBUtil.getColumnName(f))));
                            } else {
                                // FIXME: 2017/2/25 other type init
                            }
                        }
                    }
                }
                if (!cursor.isClosed()) {
                    cursor.close();
                }
                return t;
            } else {
                throw new RuntimeException("you class[" + clazz.getSimpleName() + "] must add Table annotation ");
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> ArrayList<T> queryAll(Class<T> clazz) {
        ArrayList<T> ts = new ArrayList<>();
        try {
            if (clazz.isAnnotationPresent(Table.class)) {
                String sql = "select * from " + DBUtil.getTableName(clazz);
                Cursor cursor = getDB().rawQuery(sql, null);
                T t = null;
                Field f = null;
                while (cursor.moveToNext()) {
                    t = clazz.newInstance();
                    Field[] fields = t.getClass().getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        f = fields[i];
                        f.setAccessible(true);
                        if (f.isAnnotationPresent(Column.class)) {
                            Type type = f.getType();
                            if (type == String.class) {
                                f.set(t, cursor.getString(cursor.getColumnIndex(DBUtil.getColumnName(f))));
                            } else if (type == int.class || type == Integer.class) {
                                f.set(t, cursor.getInt(cursor.getColumnIndex(DBUtil.getColumnName(f))));
                            } else {
                                // FIXME: 2017/2/25 other type init
                            }
                        }
                    }
                    ts.add(t);
                }
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } else {
                throw new RuntimeException("you class[" + clazz.getSimpleName() + "] must add Table annotation ");
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ts;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DBUtil.createTable(db, Company.class);
        DBUtil.createTable(db, Developer.class);
        DBUtil.createTable(db, Skill.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
