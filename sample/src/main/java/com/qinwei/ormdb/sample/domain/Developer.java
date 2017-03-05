package com.qinwei.ormdb.sample.domain;


import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
@Table(name = "dt_developer")
public class Developer implements Serializable {
    @Column(name = "_id", id = true)
    public String id;
    @Column
    public String name;
    @Column
    public int age;
    @Column(type = Column.ColumnType.TONE, autorefresh = true)
    public Company company;
    @Column(type = Column.ColumnType.SERIALIZABLE)
    public ArrayList<Skill> skills;


    @Override
    public String toString() {
        return "Developer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", company=" + company +
                ", skills=" + skills +
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ArrayList<Skill> getSkills() {
        return skills;
    }

    public void setSkills(ArrayList<Skill> skills) {
        this.skills = skills;
    }
}
