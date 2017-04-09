package com.qinwei.ormdb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qinwei.ormdb.cache.CacheManager;
import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;
import com.qinwei.ormdb.log.DBLog;
import com.qinwei.ormdb.util.DBUtil;
import com.qinwei.ormdb.util.SerializableUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/3/5.
 */

public class BaseDao<T> {
    private SQLiteDatabase mDatabase;//数据库
    private Class<T> mClazz;//dto object的class
    private Field[] mFields;//obj对应所有变量
    public String mIdName;//数据表id的名称
    public String mIdFieldName;//obj对应id变量名称
    public String mDataTableName;//obj对应的表名称

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

    public <T> long newOrUpdate(ContentValues values) {
        return mDatabase.replace(mDataTableName, null, values);
    }

    public <T> long newOrUpdate(T t) {
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < mFields.length; i++) {
                Field f = mFields[i];
                f.setAccessible(true);
                if (f.isAnnotationPresent(Column.class)) {
                    buildContentValues(t, values, f);
                }
            }
            return newOrUpdate(values);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private <T> void buildContentValues(T t, ContentValues values, Field f) throws IllegalAccessException {
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
                    return;
                }
                if (tone.getClass().isAnnotationPresent(Table.class)) {
                    String idValue = DBUtil.getIdValue(tone);
                    if (column.autorefresh()) {
                        long result = CacheManager.getInstance().getDao(tone.getClass()).newOrUpdate(tone);
                        values.put(columnName, idValue);
                        DBLog.d("newOrUpdate autoRefresh class [" + tone.getClass().getSimpleName() + "] result count=" + result);
                    } else {
                        values.put(columnName, idValue);
                    }
                } else {
                    throw new IllegalArgumentException("the special object [" + tone.getClass().getSimpleName() + "] must add Table Annotation");
                }
            } else if (columnType == Column.ColumnType.SERIALIZABLE) {
                values.put(columnName, SerializableUtil.toByteArray(f.get(t)));
            } else if (columnType == Column.ColumnType.INTEGER) {
                values.put(columnName, f.getInt(t));
            } else if (columnType == Column.ColumnType.BIGDECIMAL && f.get(t) != null) {
                values.put(columnName, f.get(t).toString());
            } else if (columnType == Column.ColumnType.VARCHAR) {
                values.put(columnName, f.get(t).toString());
            } else {
                // FIXME: 2017/2/25 other type
            }
        }
    }

    public T queryById(String id) {
        T t = null;
        try {
            String sql = "select * from " + mDataTableName + " where " + mIdName + "=?";
            Cursor cursor = rawQuery(sql, new String[]{id});
            ArrayList<T> ts = query(cursor);
            if (ts != null && ts.size() > 0) {
                t = ts.get(0);
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
        return t;
    }

    /**
     * 查询所有
     *
     * @return
     */
    public ArrayList<T> queryAll() {
        ArrayList<T> ts = null;
        try {
            String sql = "select * from " + mDataTableName;
            Cursor cursor = rawQuery(sql, null);
            ts = query(cursor);
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
        return ts == null ? new ArrayList<T>() : ts;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        DBLog.d("rawQuery sql:" + sql);
        return mDatabase.rawQuery(sql, selectionArgs);
    }

    private ArrayList<T> query(Cursor cursor) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        ArrayList<T> ts = new ArrayList<>();
        Field f = null;
        T t = null;
        while (cursor.moveToNext()) {
            t = mClazz.newInstance();
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
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ts;
    }

    private void dto(T t, Field f, Cursor cursor) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
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
            } else if (columnType == Column.ColumnType.TONE) {
                if (f.getType().isAnnotationPresent(Table.class)) {
                    Object tone = null;
                    String idValue = cursor.getString(cursor.getColumnIndex(columnName));
                    if (idValue == null) return;
                    if (column.autorefresh()) {//根据外键查询出并转换为object
                        tone = CacheManager.getInstance().getDao(f.getType()).queryById(idValue);
                        DBLog.d("query autoRefresh relate class[" + f.getType().getSimpleName() + "] id[" + idValue + "] info:" + tone);
                    } else {
                        //只存外键id
                        tone = f.getType().newInstance();
                        //tone id对应的变量名称
                        Field idField = tone.getClass().getDeclaredField(CacheManager.getInstance().getDao(tone.getClass()).mIdFieldName);
                        idField.set(tone, idValue);
                    }
                    f.set(t, tone);
                } else {
                    throw new IllegalArgumentException("the special object [" + f.getType().getSimpleName() + "] must add Table Annotation");
                }
            } else if (columnType == Column.ColumnType.INTEGER) {
                f.set(t, cursor.getInt(cursor.getColumnIndex(columnName)));
            } else if (columnType == Column.ColumnType.BIGDECIMAL) {
                f.set(t, new BigDecimal(cursor.getDouble(cursor.getColumnIndex(columnName))));
            } else if (columnType == Column.ColumnType.VARCHAR) {
                f.set(t, cursor.getString(cursor.getColumnIndex(columnName)));
            } else {
                // FIXME: 2017/2/25 other type
            }
        }
    }

    /**
     * 删除一个对象
     *
     * @return
     */
    public long delete(T t) {
        try {
            return delete(mIdName + "=?", new String[]{DBUtil.getIdValue(t)});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long delete(String id) {
        return delete(mIdName + "=?", new String[]{id});
    }

    public long delete(String where, String[] whereArgs) {
        return mDatabase.delete(mDataTableName, where, whereArgs);
    }
}
