package com.qinwei.ormdb;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.qinwei.ormdb.sample.db.DBHelper;
import com.qinwei.ormdb.sample.db.DBLog;
import com.qinwei.ormdb.sample.domain.Company;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
@RunWith(AndroidJUnit4.class)
public class DBTest {
    private Context appContext;
    private DBHelper mDBHelper;

    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        mDBHelper = new DBHelper(appContext);
        mDBHelper.getDB();
    }

    @Test
    public void insert() throws Exception {
        Company company = new Company();
        company.id = "10001";
        company.name = "美味不用等";
        insert(company);

        company.id = "10002";
        company.name = "城家酒店";
        insert(company);
    }

    public void insert(Company company) {
        long count = mDBHelper.newOrUpdate(company);
        if (count > 0) {
            DBLog.d("insert success！count=" + count + " data:" + company.toString());
        } else {
            DBLog.d("insert failure！count=" + count);
        }
    }

    @Test
    public void delete() throws Exception {
        Company company = new Company();
        company.id = "10001";
        company.name = "美味不用等";
        insert(company);
        long count = mDBHelper.delete(company);
        if (count > 0) {
            DBLog.d("delete success count=" + count);
        } else {
            DBLog.d("insert failure！count=" + count);
        }
    }

    @Test
    public void update() throws Exception {
        Company company = new Company();
        company.id = "10001";
        company.name = "首坦金融";
        long count = mDBHelper.newOrUpdate(company);
        if (count > 0) {
            DBLog.d("update success count=" + count);
        } else {
            DBLog.d("update failure！count=" + count);
        }
    }

    @Test
    public void queryById() throws Exception {
        Company company = mDBHelper.queryById("10001");
        if (company != null) {
            DBLog.d("queryById:" + company.toString());
        }
    }

    @Test
    public void queryAll() throws Exception {
        ArrayList<Company> companies = mDBHelper.queryAll();
        if (companies != null) {
            DBLog.d("queryById:" + companies.toString());
        }
    }

    @Test
    public void dropDB() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        appContext = null;
    }
}
