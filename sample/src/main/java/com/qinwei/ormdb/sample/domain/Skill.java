package com.qinwei.ormdb.sample.domain;

import java.io.Serializable;

/**
 * Created by qinwei on 2017/2/25.
 */

public class Skill implements Serializable {
    public String id;
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

    public void setLanguage(String language) {
        this.language = language;
    }
}
