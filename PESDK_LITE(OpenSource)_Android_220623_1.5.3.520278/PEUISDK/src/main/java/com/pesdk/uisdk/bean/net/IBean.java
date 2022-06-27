package com.pesdk.uisdk.bean.net;


import com.pesdk.bean.DataBean;

public class IBean {

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String file;

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUrl() {
        return file;
    }

    private String cover;
    private long updatetime;
    private String localPath;

    public int getMId() {
        return mId;
    }

    public void setMId(int id) {
        mId = id;
    }

    private int mId = 0;

    /**
     * @param name
     * @param file
     * @param cover
     * @param updatetime
     */
    public IBean(String name, String file, String cover, long updatetime) {
        this.name = name;
        this.file = file;
        this.cover = cover;
        this.updatetime = updatetime;
    }

    public IBean(DataBean bean) {
        this.name = bean.getName();
        this.file = bean.getFile();
        this.cover = bean.getCover();
        this.updatetime = bean.getUpdatetime();
    }

    /**
     * @param name
     * @param file
     * @param cover
     * @param localPath
     * @param updatetime
     */
    public IBean(String name, String file, String cover, String localPath, long updatetime) {
        this.name = name;
        this.file = file;
        this.cover = cover;
        this.localPath = localPath;
        this.updatetime = updatetime;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getCover() {
        return cover;
    }

    public long getUpdatetime() {
        return updatetime;
    }


    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }


    @Override
    public String toString() {
        return "IBean{" +
                "name='" + name + '\'' +
//                ", file='" + file + '\'' +
//                ", cover='" + cover + '\'' +
                ", localPath='" + localPath + '\'' +
                ", mId=" + mId +
                '}';
    }
}
