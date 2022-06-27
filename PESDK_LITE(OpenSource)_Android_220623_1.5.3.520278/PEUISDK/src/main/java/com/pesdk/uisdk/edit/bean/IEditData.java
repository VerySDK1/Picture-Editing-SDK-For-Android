package com.pesdk.uisdk.edit.bean;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;

/**
 *获取要编辑的对象
 */
public interface IEditData {
    /**
     * 编辑指定的文字
     * @param index
     * @return
     */
    WordInfoExt getWordNewInfo(int index);

    StickerInfo getStickerInfo(int index) ;

    CollageInfo getOverLay(int index);
    CollageInfo getPip(int index);



}
