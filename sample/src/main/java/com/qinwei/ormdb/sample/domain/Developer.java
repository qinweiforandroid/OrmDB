package com.qinwei.ormdb.sample.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */

public class Developer implements Serializable {
    public String id;
    public String name;
    public int age;
    public Company company;
    public ArrayList<Skill> skills;


    @Override
    public String toString() {
        return "Developer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", company=" + company +
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
