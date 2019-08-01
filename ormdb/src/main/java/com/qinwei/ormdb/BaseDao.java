package com.qinwei.ormdb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.ArrayMap;

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

public final class BaseDao<T> {
    /**
     * 数据库
     */
    private SQLiteDatabase mDatabase;
    /**
     * dto obj的class
     */
    private Class<T> mClazz;
    /**
     * obj对应所有变量
     */
    private Field[] mFields;
    /**
     * 数据表id的名称
     */
    private String mIdName;
    /**
     * obj对应id变量名称
     */
    private String mIdFieldName;
    /**
     * obj对应的表名称
     */
    private String mDataTableName;

    /**
     * obj变量名与数据库表列名映射关系集合
     */
    private ArrayMap<String, String> mFieldNameColumnNameMappers;
    /**
     * obj变量名与数据库表列名索引index映射关系集合
     */
    private ArrayMap<String, Integer> mFieldNameColumnIndexMappers;
    /**
     * obj变量名与数据库表列名索引index映射关系集合
     */
    private ArrayMap<String, Column> mFieldNameColumnInfoMappers;

    public BaseDao(SQLiteDatabase db) {
        mDatabase = db;
        mFieldNameColumnNameMappers = new ArrayMap<>();
        mFieldNameColumnIndexMappers = new ArrayMap<>();
        mFieldNameColumnInfoMappers = new ArrayMap<>();
    }

    /**
     * 设置DTO class类型
     *
     * @param clazz
     */
    public void setDTOClass(Class<T> clazz) {
        this.mClazz = clazz;
        long time = System.currentTimeMillis();
        DBLog.d("start init dao[" + getClass().getSimpleName() + "]");
        if (!mClazz.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("you class[" + mClazz.getSimpleName() + "] must add Table annotation ");
        }
        mFields = mClazz.getDeclaredFields();
        mIdName = DBUtil.getIdColumnName(mClazz);
        mIdFieldName = DBUtil.getIdFieldName(mClazz);
        mDataTableName = DBUtil.getTableName(mClazz);

        Column column = null;
        for (int i = 0; i < mFields.length; i++) {
            Field f = mFields[i];
            f.setAccessible(true);
            if (f.isAnnotationPresent(Column.class)) {
                column = f.getAnnotation(Column.class);
                mFieldNameColumnNameMappers.put(f.getName(), DBUtil.getColumnName(f, column));
                mFieldNameColumnInfoMappers.put(f.getName(), f.getAnnotation(Column.class));
            }
        }
        String sql = "select * from " + mDataTableName + " limit 1";
        Cursor cursor = rawQuery(sql, null);
        Field f = null;
        for (int i = 0; i < mFields.length; i++) {
            f = mFields[i];
            f.setAccessible(true);
            if (f.isAnnotationPresent(Column.class)) {
                mFieldNameColumnIndexMappers.put(f.getName(), cursor.getColumnIndex(DBUtil.getColumnName(f)));
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        DBLog.d("end init dao  [" + getClass().getSimpleName() + "] 耗时:" + (System.currentTimeMillis() - time));
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void endTransaction() {
        mDatabase.endTransaction();
    }

    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    public <T> long newOrUpdate(ContentValues values) {
        return newOrUpdate(values, null);
    }

    public <T> long newOrUpdate(ContentValues values, String nullColumnHack) {
        return mDatabase.replace(mDataTableName, nullColumnHack, values);
    }

    public <T> long newOrUpdate(T t) throws DBException {
        ContentValues values = new ContentValues();
        for (int i = 0; i < mFields.length; i++) {
            Field f = mFields[i];
            f.setAccessible(true);
            if (mFieldNameColumnNameMappers.containsKey(f.getName())) {
                buildContentValues(t, values, f, mFieldNameColumnNameMappers.get(f.getName()));
            }
        }
        return newOrUpdate(values);
    }

    private <T> void buildContentValues(T t, ContentValues values, Field f, String columnName) throws DBException {
        try {
            if (f.getType() == String.class) {
                values.put(columnName, DBUtil.toString(f.get(t)));
            } else if (f.getType() == int.class || f.getType() == Integer.class) {
                values.put(columnName, f.getInt(t));
            } else if (f.getType() == long.class || f.getType() == Long.class) {
                values.put(columnName, f.getLong(t));
            } else {
                Column column = mFieldNameColumnInfoMappers.get(f.getName());
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
                        BaseDao dao = CacheDaoManager.getInstance().get(mDatabase, tone.getClass());
                        String idValue = DBUtil.getIdValue(tone, dao.mIdFieldName);
                        if (column.autorefresh()) {
                            long result = CacheDaoManager.getInstance().get(mDatabase, tone.getClass()).newOrUpdate(tone);
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
                    values.put(columnName, ((BigDecimal) f.get(t)).toPlainString());
                } else if (columnType == Column.ColumnType.VARCHAR) {
                    values.put(columnName, f.get(t).toString());
                } else {
                    // FIXME: 2017/2/25 other type
                }
            }
        } catch (IllegalAccessException e) {
            throw new DBException(DBException.ErrorType.IllegalAccess, e.getMessage());
        }
    }


    private void dto(T t, Field f, Cursor cursor, int columnIndex) throws DBException {
        try {
            if (f.getType() == String.class) {
                f.set(t, cursor.getString(columnIndex));
            } else if (f.getType() == int.class || f.getType() == Integer.class) {
                f.set(t, cursor.getInt(columnIndex));
            } else if (f.getType() == long.class || f.getType() == Long.class) {
                f.set(t, cursor.getLong(columnIndex));
            } else {
                Column column = mFieldNameColumnInfoMappers.get(f.getName());
                Column.ColumnType columnType = column.type();
                if (column.type() == Column.ColumnType.UNKNOWN) {
                    throw new IllegalArgumentException("you must add columnType for special object");
                }
                if (columnType == Column.ColumnType.SERIALIZABLE) {
                    byte[] bytes = cursor.getBlob(columnIndex);
                    if (bytes != null) {
                        f.set(t, SerializableUtil.toObject(bytes));
                    } else {
                        f.set(t, null);
                    }
                } else if (columnType == Column.ColumnType.TONE) {
                    if (f.getType().isAnnotationPresent(Table.class)) {
                        BaseDao dao = CacheDaoManager.getInstance().get(mDatabase, f.getType());
                        Object tone = null;
                        String idValue = cursor.getString(columnIndex);
                        if (idValue == null) {
                            return;
                        }
                        //根据外键查询出并转换为object
                        if (column.autorefresh()) {
                            tone = dao.queryById(idValue);
                            DBLog.d("query autoRefresh relate class[" + f.getType().getSimpleName() + "] id[" + idValue + "] info:" + tone);
                        } else {
                            //只存外键id
                            tone = f.getType().newInstance();
                            //tone id对应的变量名称
                            Field idField = tone.getClass().getDeclaredField(dao.mIdFieldName);
                            idField.set(tone, idValue);
                        }
                        f.set(t, tone);
                    } else {
                        throw new IllegalArgumentException("the special object [" + f.getType().getSimpleName() + "] must add Table Annotation");
                    }
                } else if (columnType == Column.ColumnType.INTEGER) {
                    f.set(t, cursor.getInt(columnIndex));
                } else if (columnType == Column.ColumnType.BIGDECIMAL) {
                    f.set(t, new BigDecimal(cursor.getString(columnIndex)));
                } else if (columnType == Column.ColumnType.VARCHAR) {
                    f.set(t, cursor.getString(columnIndex));
                } else {
                    // FIXME: 2017/2/25 other type
                }
            }
        } catch (IllegalAccessException e) {
            throw new DBException(DBException.ErrorType.IllegalAccess, e.getMessage());
        } catch (InstantiationException e) {
            throw new DBException(DBException.ErrorType.Instantiation, e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new DBException(DBException.ErrorType.NoSuchField, e.getMessage());
        }
    }

    public T queryById(String id) throws DBException {
        T t = null;
        String sql = "select * from " + mDataTableName + " where " + mIdName + "=?";
        Cursor cursor = rawQuery(sql, new String[]{id});
        ArrayList<T> ts = query(cursor);
        if (ts != null && ts.size() > 0) {
            t = ts.get(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return t;
    }

    public ArrayList<T> queryAll() throws DBException {
        ArrayList<T> ts = null;
        String sql = "select * from " + mDataTableName;
        Cursor cursor = rawQuery(sql, null);
        ts = query(cursor);
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ts == null ? new ArrayList<T>() : ts;
    }

    public ArrayList<T> query(String sql, String[] selectionArgs) throws DBException {
        Cursor cursor = rawQuery(sql, selectionArgs);
        return query(cursor);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        DBLog.d("rawQuery sql:" + sql);
        return mDatabase.rawQuery(sql, selectionArgs);
    }

    public String queryString(String sql, String[] selectionArgs) {
        Cursor cursor = rawQuery(sql, selectionArgs);
        while (cursor.moveToNext()) {
            return cursor.getString(0);
        }
        cursor.close();
        return "";
    }

    public ArrayList<T> query(String[] columns,
                              String selection,
                              String[] selectionArgs,
                              String groupBy,
                              String having,
                              String orderBy,
                              String limit) throws DBException {
        return query(mDatabase.query(mDataTableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit));
    }


    private ArrayList<T> query(Cursor cursor) throws DBException {
        try {
            ArrayList<T> ts = new ArrayList<>();
            T t = null;
            Field f = null;
            while (cursor.moveToNext()) {
                t = mClazz.newInstance();
                for (int i = 0; i < mFields.length; i++) {
                    f = mFields[i];
                    f.setAccessible(true);
                    if (mFieldNameColumnIndexMappers.containsKey(f.getName())) {
                        dto(t, f, cursor, mFieldNameColumnIndexMappers.get(f.getName()));
                    }
                }
                ts.add(t);
            }
            return ts;
        } catch (InstantiationException e) {
            throw new DBException(DBException.ErrorType.Instantiation, e.getMessage());
        } catch (IllegalAccessException e) {
            throw new DBException(DBException.ErrorType.IllegalAccess, e.getMessage());
        } catch (SecurityException e) {
            throw new DBException(DBException.ErrorType.Security, e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public long delete(T t) throws DBException {
        try {
            return delete(mIdName + "=?", new String[]{DBUtil.getIdValue(t)});
        } catch (IllegalAccessException e) {
            throw new DBException(DBException.ErrorType.IllegalAccess, e.getMessage());
        }
    }

    public long delete(String id) {
        return delete(mIdName + "=?", new String[]{id});
    }

    public long delete(String where, String[] whereArgs) {
        return mDatabase.delete(mDataTableName, where, whereArgs);
    }
}
