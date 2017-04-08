package com.qinwei.ormdb.sample.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qinwei.ormdb.BaseDao;
import com.qinwei.ormdb.cache.CacheManager;
import com.qinwei.ormdb.sample.dao.CompanyDao;
import com.qinwei.ormdb.sample.dao.DeveloperDao;
import com.qinwei.ormdb.sample.domain.Company;
import com.qinwei.ormdb.sample.domain.Developer;
import com.qinwei.ormdb.sample.domain.Skill;
import com.qinwei.ormdb.util.DBUtil;

/**
 * Created by qinwei on 2017/2/26.
 */

public class DBManager {
    private static DBManager mInstance;
    private DBHelper mDBHelper;
    private Context mContext;

    private DBManager() {
    }

    public static DBManager getInstance() {
        if (mInstance == null) {
            mInstance = new DBManager();
        }
        return mInstance;
    }

    public void init(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        mDBHelper = new DBHelper(this.mContext);
        CacheManager.getInstance().putDao(Company.class, new CompanyDao(mDBHelper.getDB(), Company.class));
        CacheManager.getInstance().putDao(Developer.class, new DeveloperDao(mDBHelper.getDB(), Developer.class));
    }

    public <T> BaseDao<T> getDao(Class<T> clazz) {
        return (BaseDao<T>) CacheManager.getInstance().getDao(clazz);
    }

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

    public void release() {
        CacheManager.getInstance().clear();
        mContext = null;
        mDBHelper = null;
        mInstance = null;
    }
}
