package com.pesdk.bean;

import androidx.annotation.Keep;

/**
 * 单个列表数据支持分页
 */
@Keep
public class PageDataResult {


    private int code;
    private String msg;
    private PageData data;

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


    public PageData getData() {
        return data;
    }

    public void setData(PageData data) {
        this.data = data;
    }


}
