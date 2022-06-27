package com.vesdk.common.bean

class CoroutineException(
        val msg: String,
        val code: Int = 0
) : Exception(msg)