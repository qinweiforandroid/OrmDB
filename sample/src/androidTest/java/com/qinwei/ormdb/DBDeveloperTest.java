package com.qinwei.ormdb;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.qinwei.ormdb.log.DBLog;
import com.qinwei.ormdb.sample.db.DBHelper;
import com.qinwei.ormdb.sample.domain.Company;
import com.qinwei.ormdb.sample.domain.Developer;
import com.qinwei.ormdb.sample.domain.Skill;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
@RunWith(AndroidJUnit4.class)
public class DBDeveloperTest {
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        DBManager.init(appContext,new DBHelper(appContext));
    }

    @Test
    public void insert() throws Exception {
        Developer developer = new Developer();
        developer.id = "10001";
        developer.name = "刘午敬";
        developer.price = new BigDecimal(20000.01);

        Company company = new Company();
        company.id = "10001";
        company.name = "美味不用等";
        developer.company = company;//存公司信息

        ArrayList<Skill> skills = new ArrayList<>();
        Skill skill = new Skill();
        skill.name = "coding";
        skill.desc = "android";
        skills.add(skill);
        developer.skills = skills;//存技能信息
        DBManager.getInstance().getDao(Developer.class).newOrUpdate(developer);
        queryById();
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
        queryById();
    }

    @Test
    public void queryById() throws Exception {
        Developer developer = DBManager.getInstance().getDao(Developer.class).queryById("10001");
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
