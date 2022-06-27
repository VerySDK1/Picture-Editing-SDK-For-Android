package com.pesdk.bean;

import androidx.annotation.Keep;

/**
 * 上传完成
 */
@Keep
public class UploadResult {


    /**
     * code : 0
     * msg : Success
     */

    private int code;
    private String msg;

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


    @Override
    public String toString() {
        return "UploadResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
