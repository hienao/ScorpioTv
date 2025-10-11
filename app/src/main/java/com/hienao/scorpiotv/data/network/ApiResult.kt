package com.hienao.scorpiotv.data.network

/**
 * 网络请求结果的封装类
 * 用于统一处理成功、失败和加载状态
 */
sealed class ApiResult<out T> {
    /**
     * 成功状态
     * @param data 返回的数据
     */
    data class Success<T>(val data: T) : ApiResult<T>()
    
    /**
     * 失败状态
     * @param exception 异常信息
     * @param message 错误消息
     * @param code 错误码
     */
    data class Error(
        val exception: Throwable? = null,
        val message: String? = null,
        val code: Int? = null
    ) : ApiResult<Nothing>()
    
    /**
     * 加载中状态
     */
    data object Loading : ApiResult<Nothing>()
    
    /**
     * 判断是否成功
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * 判断是否失败
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * 判断是否加载中
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * 获取数据,如果是Success则返回数据,否则返回null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * 获取异常,如果是Error则返回异常,否则返回null
     */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }
    
    /**
     * 转换数据类型
     */
    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
    
    /**
     * 当成功时执行操作
     */
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * 当失败时执行操作
     */
    inline fun onError(action: (Throwable?, String?, Int?) -> Unit): ApiResult<T> {
        if (this is Error) action(exception, message, code)
        return this
    }
    
    /**
     * 当加载中时执行操作
     */
    inline fun onLoading(action: () -> Unit): ApiResult<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * 扩展函数:将可能抛出异常的操作包装为ApiResult
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: Exception) {
    ApiResult.Error(
        exception = e,
        message = e.message ?: "Unknown error occurred"
    )
}