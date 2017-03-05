package com.qinwei.ormdb.sample.dao;

import android.database.sqlite.SQLiteDatabase;

import com.qinwei.ormdb.BaseDao;
import com.qinwei.ormdb.sample.domain.Developer;

/**
 * Created by qinwei on 2017/3/5.
 */

public class DeveloperDao extends BaseDao<Developer> {
    public DeveloperDao(SQLiteDatabase db, Class<Developer> clz) {
        super(db, clz);
    }
}
