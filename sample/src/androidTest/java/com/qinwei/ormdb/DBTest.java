package com.qinwei.ormdb;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.qinwei.ormdb.sample.db.DBHelper;
import com.qinwei.ormdb.sample.db.DBLog;
import com.qinwei.ormdb.sample.db.DBManager;
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

    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        DBManager.getInstance(appContext);
    }

    @Test
    public void insert() throws Exception {
        Company company = new Company();
        company.id = "10001";
        company.name = "美味不用等";
        DBManager.getInstance(appContext).newOrUpdate(company);

        company.id = "10002";
        company.name = "城家酒店";
        DBManager.getInstance(appContext).newOrUpdate(company);
    }

    @Test
    public void delete() throws Exception {
        Company company = new Company();
        company.id = "10001";
        company.name = "美味不用等";
        DBManager.getInstance(appContext).delete(company);
    }

    @Test
    public void update() throws Exception {
        Company company = new Company();
        company.id = "10001";
        company.name = "首坦金融";
        DBManager.getInstance(appContext).newOrUpdate(company);
    }

    @Test
    public void queryById() throws Exception {
        Company company = DBManager.getInstance(appContext).queryById("10001", Company.class);
        DBLog.d("queryById:" + company.toString());
    }

    @Test
    public void queryAll() throws Exception {
        ArrayList<Company> companies = DBManager.getInstance(appContext).queryAll(Company.class);
        DBLog.d("queryAll:" + companies.toString());
    }

    @Test
    public void dropDB() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        appContext = null;
    }
}
