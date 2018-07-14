package com.qinwei.ormdb.cache;

import com.qinwei.ormdb.BaseDao;

import java.util.HashMap;

/**
 * Created by qinwei on 2017/3/6.
 */

public class CacheManager {
    private static CacheManager mInstance;
    private HashMap<String, BaseDao> daoCache;

    private CacheManager() {
        daoCache = new HashMap<>();
    }

    public static CacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new CacheManager();
        }
        return mInstance;
    }

    public void putDao(Class dtoClass, BaseDao<?> baseDao) {
        daoCache.put(dtoClass.getSimpleName(), baseDao);
    }

    public <T> BaseDao<T> getDao(Class<T> dtoClass) {
        return (BaseDao<T>) daoCache.get(dtoClass.getSimpleName());
    }

    public void clear() {
        daoCache.clear();
    }
}
