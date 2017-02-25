package com.qinwei.ormdb.sample.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qinwei.ormdb.sample.domain.Company;

import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ormDB.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public SQLiteDatabase getDB() {
        return getWritableDatabase();
    }

    public long newOrUpdate(Company company) {
        ContentValues values = new ContentValues();
        values.put(Company.COLUMN_ID, company.id);
        values.put(Company.COLUMN_NAME, company.name);
        return getDB().replace(Company.TABLE_COMPANY, null, values);
    }

    public long delete(Company company) {
        return getDB().delete(Company.TABLE_COMPANY, Company.COLUMN_ID + "=?", new String[]{company.id});
    }

    public Company queryById(String id) {
        String sql = "select * from " + Company.TABLE_COMPANY + " where " + Company.COLUMN_ID + "=?";
        Cursor cursor = getDB().rawQuery(sql, new String[]{id});
        Company company = null;
        while (cursor.moveToNext()) {
            company = new Company();
            company.id = cursor.getString(cursor.getColumnIndex(Company.COLUMN_ID));
            company.name = cursor.getString(cursor.getColumnIndex(Company.COLUMN_NAME));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return company;
    }

    public ArrayList queryAll() {
        String sql = "select * from " + Company.TABLE_COMPANY;
        Cursor cursor = getDB().rawQuery(sql, null);
        Company company = null;
        ArrayList<Company> companies = new ArrayList<>();
        while (cursor.moveToNext()) {
            company = new Company();
            company.id = cursor.getString(cursor.getColumnIndex(Company.COLUMN_ID));
            company.name = cursor.getString(cursor.getColumnIndex(Company.COLUMN_NAME));
            companies.add(company);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return companies;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_company = "create table dt_company (_id varchar primary key,name varchar);";
        String sql_developer = "create table dt_developer (_id varchar primary key,name varchar,age integer,company_id varchar);";
        String sql_skill = "create table dt_skill (_id varchar primary key,language varchar);";
        db.execSQL(sql_company);
        db.execSQL(sql_developer);
        db.execSQL(sql_skill);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
