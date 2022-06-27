package com.pesdk.bean;

import java.util.List;

import androidx.annotation.Keep;

/**
 * 分组接口返回
 */
@Keep
public class SortResult {

    private int code;
    private String msg;
    private List<SortBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<SortBean> getData() {
        return data;
    }

    public void setData(List<SortBean> data) {
        this.data = data;
    }


}
