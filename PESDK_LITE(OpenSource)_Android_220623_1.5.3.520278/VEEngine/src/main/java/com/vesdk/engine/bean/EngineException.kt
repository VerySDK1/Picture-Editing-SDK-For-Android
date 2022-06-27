package com.vesdk.engine.bean

class EngineException(
        val code: Int,
        val msg: String
) : Exception()