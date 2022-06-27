package com.pesdk.uisdk.bean.net;

public class WebFilterInfo extends IBean {
    /**
     * 素材Id
     */
    private String id;
    //素材分组Id
    private String groupId;

    public String getGroupId() {
        return groupId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    private boolean startItem, endItem;


    public boolean isStartItem() {
        return startItem;
    }

    public void setStartItem(boolean startItem) {
        this.startItem = startItem;
    }

    public boolean isEndItem() {
        return endItem;
    }

    public void setEndItem(boolean endItem) {
        this.endItem = endItem;
    }


    public WebFilterInfo(String sortId, String id, String url, String img, String name, String _localPath, long updatetime) {
        super(name, url, img, _localPath, updatetime);
        this.id = id;
        this.groupId = sortId;
    }

    @Override
    public String toString() {
        return "WebFilterInfo{" +
                "id=" + id +
                " super=" + super.toString() +
                ", groupId='" + groupId + '\'' +
                ", startItem=" + startItem +
                ", endItem=" + endItem +
                '}';
    }
}
