package com.qinwei.ormdb.sample.domain;

import com.qinwei.ormdb.sample.db.Column;
import com.qinwei.ormdb.sample.db.Table;

import java.io.Serializable;

/**
 * Created by qinwei on 2017/2/25.
 */
@Table(name = "dt_skill")
public class Skill implements Serializable {
    @Column(name = "_id", id = true)
    public String id;
    @Column
    public String language;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "id='" + id + '\'' +
                ", language='" + language + '\'' +
                '}';
    }

    public void setLanguage(String language) {

        this.language = language;
    }
}
