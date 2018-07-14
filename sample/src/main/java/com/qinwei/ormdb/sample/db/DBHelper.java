package com.qinwei.ormdb.sample.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qinwei.ormdb.sample.domain.Company;
import com.qinwei.ormdb.sample.domain.Developer;
import com.qinwei.ormdb.sample.domain.Skill;
import com.qinwei.ormdb.util.DBUtil;

/**
 * Created by qinwei on 2018/7/14.
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
