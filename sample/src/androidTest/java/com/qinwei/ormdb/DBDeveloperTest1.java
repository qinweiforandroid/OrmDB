package com.qinwei.ormdb;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.qinwei.ormdb.log.DBLog;
import com.qinwei.ormdb.sample.db.DBHelper;
import com.qinwei.ormdb.sample.domain.Company;
import com.qinwei.ormdb.sample.domain.Developer;
import com.qinwei.ormdb.sample.domain.Skill;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
@RunWith(AndroidJUnit4.class)
public class DBDeveloperTest1 {
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getContext();
        DBManager.init(new DBHelper(appContext));
    }

    @Test
    public void insert() throws Exception {
        final Developer developer = new Developer();
        developer.id = "y10001";
        developer.name = "刘午敬";

        Company company = new Company();
        company.id = "g10001";
        company.name = "美味不用等";
        developer.company = company;//存公司信息

        ArrayList<Skill> skills = new ArrayList<>();
        Skill skill = new Skill();
        skill.name = "coding";
        skill.desc = "android";
        skills.add(skill);
        developer.skills = skills;//存技能信息

        long time = System.currentTimeMillis();
        DBLog.d("----------start insert-------------");
        DBManager.getInstance().getDao(Developer.class).beginTransaction();
        for (int i = 0; i < 10000; i++) {
            developer.id = "" + i;
            developer.company.id = "" + i;
            DBManager.getInstance().getDao(Developer.class).newOrUpdate(developer);
        }
        DBManager.getInstance().getDao(Developer.class).setTransactionSuccessful();
        DBManager.getInstance().getDao(Developer.class).endTransaction();
        DBLog.d("----------end insert------------- 耗时：" + (System.currentTimeMillis() - time));
//        queryById();
    }

    @Test
    public void query() throws Exception {
        String sql = "select * from dt_developer limit 1000";
        long time = System.currentTimeMillis();
        DBLog.d("----------start query-------------");
        ArrayList<Developer> developers = DBManager.getInstance().getDao(Developer.class).query(sql, null);
        DBLog.d(developers.size() + " ----------end query------------- 耗时：" + (System.currentTimeMillis() - time));
    }

    @Test
    public void delete() throws Exception {
        Developer developer = new Developer();
        developer.id = "10001";
        DBManager.getInstance().getDao(Developer.class).delete(developer);
    }

    @Test
    public void update() throws Exception {
        Developer developer = new Developer();
        developer.id = "10001";
        developer.name = "张三";
        DBManager.getInstance().getDao(Developer.class).newOrUpdate(developer);
    }

    @Test
    public void queryById() throws Exception {
        Developer developer = DBManager.getInstance().getDao(Developer.class).queryById("y10001");
        DBLog.d("queryById:" + developer.toString());
    }

    @Test
    public void queryAll() throws Exception {
        ArrayList<Developer> developers = DBManager.getInstance().getDao(Developer.class).queryAll();
        DBLog.d("queryAll:" + developers.toString());
    }

    @Test
    public void dropDB() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        appContext = null;
    }
}
