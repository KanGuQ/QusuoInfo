package com.xinghe.stellaris.http

class ResponseWrapper<T>(
        val status: Int = 0,
        val message: String = "",
        val data: T? = null
) {
    companion object {
        fun <T> ok(data: T): ResponseWrapper<T> {
            return ResponseWrapper(0, "", data)
        }
        fun <T> notFound(): ResponseWrapper<T> {
            return ResponseWrapper(404, "查询不到目标", null)
        }
    }
}