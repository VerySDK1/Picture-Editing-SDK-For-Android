package com.vesdk.common.bean

import androidx.annotation.Keep

@Keep
data class Permission(
        /**
         * 图标
         */
        var icon: Int,
        /**
         * 权限
         */
        val key: String,
        /**
         * 标题
         */
        val title: String,
        /**
         * 备忘录
         */
        val memo: String,
        /**
         * 等级
         */
        val level: Int,
)