package com.qinwei.ormdb.sample.dao;

import com.qinwei.ormdb.sample.db.DBManager;
import com.qinwei.ormdb.sample.domain.Company;

/**
 * Created by qinwei on 2017/3/5.
 */

public class DTOCompanyController {
    public static CompanyDao getDao() {
        return (CompanyDao) DBManager.getInstance().getDao(Company.class);
    }


    public static long newOrUpdate(Company company) {
        return getDao().newOrUpdate(company);
    }

    public static long delete(String id) {
        return getDao().delete(id);
    }
}
