package com.pesdk.uisdk.bean.model;


import com.pesdk.bean.DataBean;

/**
 * 背景图
 */
public class ItemBean extends DataBean {

    public String getSortId() {
        return mSortId;
    }

    public void setSortId(String sortId) {
        mSortId = sortId;
    }

    private String mSortId; //分组Id
    private String localPath;

    public ItemBean(DataBean bean) {
        super(bean);
    }

    public ItemBean() {
        super();
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String toString() {
        return "IStyle{" +
                "localPath='" + localPath + '\'' +
                '}';
    }
}
