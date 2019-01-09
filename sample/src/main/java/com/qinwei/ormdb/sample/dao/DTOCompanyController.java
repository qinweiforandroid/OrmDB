package com.qinwei.ormdb.sample.dao;

import com.qinwei.ormdb.BaseDao;
import com.qinwei.ormdb.DBException;
import com.qinwei.ormdb.DBManager;
import com.qinwei.ormdb.sample.domain.Company;

/**
 * Created by qinwei on 2017/3/5.
 */

public class DTOCompanyController {
    public static BaseDao<Company> getDao() {
        return DBManager.getInstance().getDao(Company.class);
    }

    public static long newOrUpdate(Company company) {
        return getDao().newOrUpdate(company);
    }

    public static long delete(String id) {
        return getDao().delete(id);
    }

    public static Company queryById(String id) {
        return getDao().queryById(id);
    }
}
