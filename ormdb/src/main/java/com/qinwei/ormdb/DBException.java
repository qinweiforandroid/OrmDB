package com.qinwei.ormdb;

/**
 * Created by qinwei on 2018/12/28 2:07 PM
 * email: qin.wei@mwee.cn
 */

public class DBException extends RuntimeException {
    private ErrorType type;

    public enum ErrorType {
        IllegalAccess, Instantiation, Security, NoSuchField, IllegalArgument, UNKNOW
    }

    private int code;

    public DBException(String message) {
        super(message);
    }

    public DBException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public DBException(int code, String message) {
        super(message);
        this.code = code;
    }

}
