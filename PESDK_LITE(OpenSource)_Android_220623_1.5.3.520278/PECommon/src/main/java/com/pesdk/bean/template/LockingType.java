package com.pesdk.bean.template;

import androidx.annotation.Keep;

@Keep
public enum LockingType {

    //类型  0表示不限制  1表示仅图片  2表示仅视频  3锁定
    LockingNone, LockingImage, LockingVideo, Locking,

}