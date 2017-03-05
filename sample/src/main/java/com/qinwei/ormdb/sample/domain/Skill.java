package com.qinwei.ormdb.sample.domain;


import com.qinwei.ormdb.core.Column;
import com.qinwei.ormdb.core.Table;

import java.io.Serializable;

/**
 * Created by qinwei on 2017/2/25.
 */
@Table(name = "dt_skill")
public class Skill implements Serializable {
    @Column(name = "_id", id = true)
    public String id;
    @Column
    public String name;
    @Column
    public String desc;

    @Override
    public String toString() {
        return "Skill{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
