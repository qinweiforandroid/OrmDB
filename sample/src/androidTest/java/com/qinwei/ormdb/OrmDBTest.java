package com.qinwei.ormdb;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.qinwei.ormdb.sample.db.DBHelper;
import com.qinwei.ormdb.sample.db.DBLog;
import com.qinwei.ormdb.sample.domain.Skill;
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
public class OrmDBTest {
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
        Skill skill = new Skill();
        skill.id = "10001";
        skill.language = "java";
        insert(skill);

        skill.id = "10002";
        skill.language = "c#";
        insert(skill);
    }

    public void insert(Skill skill) {
        long count = mDBHelper.newOrUpdate(skill);
        if (count > 0) {
            DBLog.d("insert success！count=" + count + " data:" + skill.toString());
        } else {
            DBLog.d("insert failure！count=" + count);
        }
    }

    @Test
    public void delete() throws Exception {
        Skill skill = new Skill();
        skill.id = "10001";
        skill.language = "美味不用等";
        long count = mDBHelper.delete(skill);
        if (count > 0) {
            DBLog.d("delete success count=" + count);
        } else {
            DBLog.d("insert failure！count=" + count);
        }
    }

    @Test
    public void update() throws Exception {
        Skill Skill = new Skill();
        Skill.id = "10001";
        Skill.language = "ios";
        long count = mDBHelper.newOrUpdate(Skill);
        if (count > 0) {
            DBLog.d("update success count=" + count);
        } else {
            DBLog.d("update failure！count=" + count);
        }
    }

    @Test
    public void queryById() throws Exception {
        Skill skill = mDBHelper.queryById("10001", Skill.class);
        if (skill != null) {
            DBLog.d("queryById:" + skill.toString());
        }
    }

    @Test
    public void queryAll() throws Exception {
        ArrayList<Skill> skills = mDBHelper.queryAll(Skill.class);
        if (skills != null) {
            DBLog.d("queryById:" + skills.toString());
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
