package com.qinwei.ormdb;

import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

/**
 * Created by qinwei on 2018/11/29.
 */

public class CacheDaoManager {
    private static CacheDaoManager mInstance;
    private HashMap<String, BaseDao> mCachedDaos;

    private CacheDaoManager() {
        mCachedDaos = new HashMap<>();
    }


    public static CacheDaoManager getInstance() {
        if (mInstance == null) {
            mInstance = new CacheDaoManager();
        }
        return mInstance;
    }

    public <T> BaseDao<T> get(SQLiteDatabase mDatabase, Class clz) {
        BaseDao<T> dao = mCachedDaos.get(mDatabase.getPath() + clz.getSimpleName());
        if (dao == null) {
            dao = new BaseDao<>(mDatabase);
            dao.setDTOClass(clz);
            mCachedDaos.put(mDatabase.getPath() + clz.getSimpleName(), dao);
        }
        return dao;
    }
}
