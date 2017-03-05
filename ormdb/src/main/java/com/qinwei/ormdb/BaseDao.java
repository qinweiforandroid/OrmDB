package com.qinwei.ormdb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qinwei.ormdb.cache.DaoCacheManager;
import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;
import com.qinwei.ormdb.log.DBLog;
import com.qinwei.ormdb.util.DBUtil;
import com.qinwei.ormdb.util.SerializableUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/3/5.
 */

public class BaseDao<T> {
    private SQLiteDatabase mDatabase;//数据库
    private Class<T> mClazz;//dto object的class
    private Field[] mFields;//obj对应所有变量
    private String mIdName;//数据表id的名称
    private String mIdFieldName;//obj对应id变量名称
    private String mDataTableName;//obj对应的表名称

    public BaseDao(SQLiteDatabase db, Class<T> clz) {
        mDatabase = db;
        mClazz = clz;
        mFields = clz.getDeclaredFields();
        mIdName = DBUtil.getIdColumnName(clz);
        mIdFieldName = DBUtil.getIdFieldName(clz);
        mDataTableName = DBUtil.getTableName(clz);
        if (!mClazz.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("you class[" + clz.getSimpleName() + "] must add Table annotation ");
        }
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void endTransaction() {
        mDatabase.endTransaction();
    }

    public <T> long newOrUpdate(T t) {
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < mFields.length; i++) {
                Field f = mFields[i];
                f.setAccessible(true);
                if (f.isAnnotationPresent(Column.class)) {
                    String columnName = DBUtil.getColumnName(f);
                    if (f.getType() == String.class) {
                        values.put(columnName, f.get(t).toString());
                    } else if (f.getType() == int.class || f.getType() == Integer.class) {
                        values.put(columnName, f.getInt(t));
                    } else {
                        Column column = f.getAnnotation(Column.class);
                        Column.ColumnType columnType = column.type();
                        if (columnType == Column.ColumnType.UNKNOWN) {
                            throw new IllegalArgumentException("you must add columnType for special object");
                        }
                        //一对一关系处理
                        if (columnType == Column.ColumnType.TONE) {
                            Object tone = f.get(t);
                            if (tone == null) {
                                continue;
                            }
                            if (tone.getClass().isAnnotationPresent(Table.class)) {
                                if (column.autorefresh()) {
                                    long result = DaoCacheManager.getInstance().getDao(tone.getClass()).newOrUpdate(tone);
                                    DBLog.d("newOrUpdate autoRefresh class [" + tone.getClass().getSimpleName() + "] result count=" + result);
                                } else {
                                    String idValue = DBUtil.getIdValue(tone);
                                    values.put(mIdName, idValue);
                                }
                            } else {
                                throw new IllegalArgumentException("the special object [" + tone.getClass().getSimpleName() + "] must add Table Annotation");
                            }
                        } else if (columnType == Column.ColumnType.SERIALIZABLE) {
                            values.put(columnName, SerializableUtil.toByteArray(f.get(t)));
                        } else {
                            // FIXME: 2017/2/25 other type
                        }
                    }
                }
            }
            return mDatabase.replace(mDataTableName, null, values);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public T queryById(String id) {
        try {
            T t = null;
            String sql = "select * from " + mDataTableName + " where " + DBUtil.getIdColumnName(mClazz) + "=?";
            Cursor cursor = mDatabase.rawQuery(sql, new String[]{id});
            Field f = null;
            while (cursor.moveToNext()) {
                t = mClazz.newInstance();
                for (int i = 0; i < mFields.length; i++) {
                    f = mFields[i];
                    f.setAccessible(true);
                    if (f.isAnnotationPresent(Column.class)) {
                        Type type = f.getType();
                        if (type == String.class) {
                            f.set(t, cursor.getString(cursor.getColumnIndex(DBUtil.getColumnName(f))));
                        } else if (type == int.class || type == Integer.class) {
                            f.set(t, cursor.getInt(cursor.getColumnIndex(DBUtil.getColumnName(f))));
                        } else {
                            Column column = f.getAnnotation(Column.class);
                            Column.ColumnType columnType = column.type();
                            if (columnType == Column.ColumnType.UNKNOWN) {
                                throw new IllegalArgumentException("you must add columnType for special object");
                            }
                            if (columnType == Column.ColumnType.SERIALIZABLE) {
                                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(DBUtil.getColumnName(f)));
                                if (bytes != null) {
                                    f.set(t, SerializableUtil.toObject(bytes));
                                } else {
                                    f.set(t, null);
                                }
                            } else if (columnType == Column.ColumnType.TONE) {
                                if (f.getType().isAnnotationPresent(Table.class)) {
                                    Object tone = null;
                                    String idName = DBUtil.getIdColumnName(f.getType());
                                    String idValue = cursor.getString(cursor.getColumnIndex(idName));
                                    if (column.autorefresh()) {//根据外键查询出并转换为object
//                                            tone = queryById(idValue, f.getType());
                                        tone = DaoCacheManager.getInstance().getDao(f.getType()).queryById(idValue);
                                        DBLog.d("query autoRefresh relate class[" + f.getType().getSimpleName() + "] id[" + idValue + "] info:" + tone);
                                    } else {
                                        //只存外键id
                                        tone = f.getType().newInstance();
                                        //tone id对应的变量名称
                                        String idFieldName = DBUtil.getIdFieldName(tone.getClass());
                                        Field idField = tone.getClass().getDeclaredField(idFieldName);
                                        idField.set(tone, idValue);
                                    }
                                    f.set(t, tone);
                                } else {
                                    throw new IllegalArgumentException("the special object [" + f.getType().getSimpleName() + "] must add Table Annotation");
                                }
                            } else {
                                // FIXME: 2017/2/25 other type
                            }
                        }
                    }
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有
     *
     * @return
     */
    public ArrayList<T> queryAll() {
        ArrayList<T> ts = new ArrayList<>();
        try {
            String sql = "select * from " + mDataTableName;
            Cursor cursor = mDatabase.rawQuery(sql, null);
            T t = null;
            Field f = null;
            while (cursor.moveToNext()) {
                t = mClazz.newInstance();
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
                            Column column = f.getAnnotation(Column.class);
                            Column.ColumnType columnType = column.type();
                            if (columnType == Column.ColumnType.UNKNOWN) {
                                throw new IllegalArgumentException("you must add columnType for special object");
                            }
                            if (columnType == Column.ColumnType.SERIALIZABLE) {
                                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(DBUtil.getColumnName(f)));
                                if (bytes != null) {
                                    f.set(t, SerializableUtil.toObject(bytes));
                                } else {
                                    f.set(t, null);
                                }
                            } else if (columnType == Column.ColumnType.TONE) {
                                if (f.getType().isAnnotationPresent(Table.class)) {
                                    Object tone = null;
                                    String idName = DBUtil.getIdColumnName(f.getType());
                                    String idValue = cursor.getString(cursor.getColumnIndex(idName));
                                    if (column.autorefresh()) {//根据外键查询出并转换为object
//                                            tone = queryById(idValue, f.getType());
                                        tone = DaoCacheManager.getInstance().getDao(f.getType()).queryById(idValue);
                                        DBLog.d("query autoRefresh relate class[" + f.getType().getSimpleName() + "] id[" + idValue + "] info:" + tone);
                                    } else {
                                        //只存外键id
                                        tone = f.getType().newInstance();
                                        //tone id对应的变量名称
                                        String idFieldName = DBUtil.getIdFieldName(tone.getClass());
                                        Field idField = tone.getClass().getDeclaredField(idFieldName);
                                        idField.set(tone, idValue);
                                    }
                                    f.set(t, tone);
                                } else {
                                    throw new IllegalArgumentException("the special object [" + f.getType().getSimpleName() + "] must add Table Annotation");
                                }
                            } else {
                                // FIXME: 2017/2/25 other type
                            }
                        }
                    }
                }
                ts.add(t);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return ts;
    }

    /**
     * 删除一个对象
     *
     * @return
     */
    public long delete(T t) {
        try {
            return mDatabase.delete(mDataTableName, mIdName + "=?", new String[]{DBUtil.getIdValue(t)});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
