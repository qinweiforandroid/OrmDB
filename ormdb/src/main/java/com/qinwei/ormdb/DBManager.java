package com.qinwei.ormdb;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qinwei.ormdb.util.DBUtil;

public class DBManager {
    private static DBManager mInstance;
    private final SQLiteDatabase mDatabase;


    private DBManager(SQLiteOpenHelper helper) {
        mDatabase = helper.getWritableDatabase();
    }

    public static void init(SQLiteOpenHelper helper) {
        if (mInstance == null) {
            mInstance = new DBManager(helper);
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

    public <T> BaseDao<T> getDao(Class<T> clz) {
        return DBUtil.getDao(mDatabase, clz);
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
}