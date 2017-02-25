package com.qinwei.ormdb.sample.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
public class Company implements Serializable {
    public static final String TABLE_COMPANY = "dt_company";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public String id;
    public String name;
    public ArrayList<Developer> developers;

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Developer> getDevelopers() {
        return developers;
    }

    public void setDevelopers(ArrayList<Developer> developers) {
        this.developers = developers;
    }
}
