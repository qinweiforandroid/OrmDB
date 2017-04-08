package com.qinwei.ormdb.sample.domain;


import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
@Table(name = "dt_company")
public class Company implements Serializable {
    @Column(name = "_id", id = true)
    public String id;
    @Column(type = Column.ColumnType.TEXT)
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
