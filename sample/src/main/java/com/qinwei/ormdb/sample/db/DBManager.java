package com.qinwei.ormdb.sample.db;

import android.content.Context;
import android.database.Cursor;

import com.qinwei.ormdb.BaseDao;
import com.qinwei.ormdb.cache.CacheManager;
import com.qinwei.ormdb.sample.dao.CompanyDao;
import com.qinwei.ormdb.sample.dao.DeveloperDao;
import com.qinwei.ormdb.sample.domain.Company;
import com.qinwei.ormdb.sample.domain.Developer;

import java.util.ArrayList;

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
        CacheManager.getInstance().putDao(Company.class, new CompanyDao(mDBHelper.getDB()));
        CacheManager.getInstance().putDao(Developer.class, new DeveloperDao(mDBHelper.getDB()));
    }

    public <T> BaseDao<T> getDao(Class<T> clazz) {
        BaseDao<T> baseDao = CacheManager.getInstance().getDao(clazz);
        if (baseDao == null) {
            baseDao = new BaseDao<T>(mDBHelper.getDB());
            CacheManager.getInstance().putDao(clazz, baseDao);
        }
        return baseDao;
    }

    public <T> long newOrUpdate(T t) {
        return getDao(t.getClass()).newOrUpdate(t);
    }

    public <T> long delete(Class<T> clzz, String where, String[] whereArgs) {
        return getDao(clzz).delete(where, whereArgs);
    }

    public <T> long delete(Class<T> clzz, String id) {
        return getDao(clzz).delete(id);
    }

    public <T> ArrayList<T> queryAll(Class<T> clzz) {
        return getDao(clzz).queryAll();
    }

    public <T> Cursor rawQuery(Class<T> clzz, String sql, String[] selectionArgs) {
        return getDao(clzz).rawQuery(sql, selectionArgs);
    }

    public void release() {
        CacheManager.getInstance().clear();
        mContext = null;
        mDBHelper = null;
        mInstance = null;
    }
}
