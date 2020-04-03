package com.weblog.content.common;

import java.io.Serializable;
public class WblogContentResult<T> implements Serializable {
    String desc;
    int code; //200成功,400失败;
    T result;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}