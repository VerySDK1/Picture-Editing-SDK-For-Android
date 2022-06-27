package com.vesdk.common.helper

import com.vesdk.common.utils.MMKVUtils

object MMKVHelper {

    /**
     * 皮肤key
     */
    private const val APP_SKIN_NAME = "appSkinName"

    /**
     * 皮肤名字
     */
    fun getAppSKinName(): String {
        return MMKVUtils.decodeStringDef(APP_SKIN_NAME)
    }

    /**
     * 保存皮肤名字
     */
    fun saveAppSKinName(name: String) {
        MMKVUtils.encode(APP_SKIN_NAME, name)
    }


}