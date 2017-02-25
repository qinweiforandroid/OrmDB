package com.qinwei.ormdb.sample.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by qinwei on 2017/2/25.
 */
public class Company implements Serializable {
    public String id;
    public String name;
    public ArrayList<Developer> developers;
}
