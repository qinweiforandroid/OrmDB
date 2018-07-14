package com.qinwei.ormdb.sample.dao;

import android.database.sqlite.SQLiteDatabase;

import com.qinwei.ormdb.BaseDao;
import com.qinwei.ormdb.sample.domain.Company;

import java.util.ArrayList;

/**
 * Created by qinwei on 2017/3/5.
 */

public class CompanyDao extends BaseDao<Company> {
    public CompanyDao(SQLiteDatabase db) {
        super(db);
    }

    public ArrayList<Company> queryCustom() {
        return null;
    }
}
