package com.qinwei.ormdb.cache;

import com.qinwei.ormdb.BaseDao;

import java.util.HashMap;

/**
 * Created by qinwei on 2017/3/6.
 */

public class DaoCacheManager {
    private static DaoCacheManager mInstance;
    private HashMap<String, BaseDao> daoCache;

    private DaoCacheManager() {
        daoCache = new HashMap<>();
    }

    public static DaoCacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new DaoCacheManager();
        }
        return mInstance;
    }

    public void putDao(Class dtoClass, BaseDao<?> baseDao) {
        daoCache.put(dtoClass.getSimpleName(), baseDao);
    }

    public BaseDao<?> getDao(Class dtoClass) {
        return daoCache.get(dtoClass.getSimpleName());
    }

    public void clear() {
        daoCache.clear();
    }
}
