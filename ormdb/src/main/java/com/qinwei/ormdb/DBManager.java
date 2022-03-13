package com.qinwei.ormdb;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qinwei.ormdb.util.DBUtil;

import java.time.chrono.MinguoDate;
import java.util.ArrayList;

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

    public <T> BaseDao<T> getDao(@NonNull Class<T> clz) {
        return DBUtil.getDao(mDatabase, clz);
    }

    public void endTransaction() {
        mDatabase.endTransaction();
    }

    public void execSQL(@NonNull String sql) {
        mDatabase.execSQL(sql);
    }

    public void execSQL(@NonNull String sql, @Nullable Object[] bindArgs) {
        mDatabase.execSQL(sql, bindArgs);
    }

    @NonNull
    public String queryString(@NonNull Class clazz,
                              @NonNull String sql,
                              @Nullable String[] selectionArgs) {
        return DBUtil.queryString(mDatabase, clazz, sql, selectionArgs);
    }

    @NonNull
    public ArrayList<String> queryArrayString(@NonNull Class clazz,
                                              @NonNull String sql,
                                              @Nullable String[] selectionArgs) {
        return DBUtil.queryArrayString(mDatabase, clazz, sql, selectionArgs);
    }

    public void release() {
        mDatabase.close();
        mInstance = null;
    }
}