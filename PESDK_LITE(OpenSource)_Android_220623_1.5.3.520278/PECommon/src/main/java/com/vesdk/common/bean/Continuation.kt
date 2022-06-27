package com.vesdk.common.bean

import androidx.annotation.Keep

/**
 * java调用
 */
@Keep
abstract class Continuation<in T> : kotlin.coroutines.Continuation<T> {

    override fun resumeWith(result: Result<T>) = result.fold(::resume, ::resumeWithException)

    /**
     * 拿数据
     */
    abstract fun resume(value: T)

    /**
     * 处理异常
     */
    abstract fun resumeWithException(exception: Throwable)
}