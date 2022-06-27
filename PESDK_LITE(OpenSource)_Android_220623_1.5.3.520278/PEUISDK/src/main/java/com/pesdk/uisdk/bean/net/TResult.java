package com.pesdk.uisdk.bean.net;

import java.util.List;

/**
 *
 */
public class TResult<E> {
    String groupId;
    List<E> mList;

    public TResult(String groupId, List<E> list) {

        this.groupId = groupId;
        mList = list;
    }


    public String getGroupId() {
        return groupId;
    }

    public List<E> getList() {
        return mList;
    }
}
