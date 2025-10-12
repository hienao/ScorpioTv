package com.hienao.scorpiotv.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Ktor网络客户端配置
 * 提供统一的HTTP客户端实例和配置
 */
object NetworkClient {
    
    /**
     * 创建配置好的HttpClient实例
     * @param enableLogging 是否启用日志
     * @param timeout 超时时间(毫秒)
     * @param storeCookies 是否存储Cookie，默认为true
     */
    fun createHttpClient(
        enableLogging: Boolean = true,
        timeout: Long = 30_000L,
        storeCookies: Boolean = true
    ): HttpClient {
        return HttpClient(Android) {
            // 超时配置
            install(HttpTimeout) {
                requestTimeoutMillis = timeout
                connectTimeoutMillis = timeout
                socketTimeoutMillis = timeout
            }
            
            // 内容协商 - JSON序列化
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            
            // 日志配置
            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            android.util.Log.d("KtorClient", message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }
            
            // 默认请求配置
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            
            // Cookie存储配置
            if (!storeCookies) {
                // 禁用Cookie存储，用于公开API请求
                // 注意：Ktor的HttpSend插件API在不同版本中可能有变化
                // 这里使用更简单的方式，通过请求拦截器移除Cookie
                defaultRequest {
                    headers.remove(HttpHeaders.Cookie)
                }
            }
        }
    }
}

/**
 * 网络请求扩展函数
 */
class KtorClient(val httpClient: HttpClient) {
    
    /**
     * GET请求
     * @param url 请求URL
     * @param headers 请求头
     * @param params 查询参数
     * @param authCookie 认证Cookie（可选）
     */
    suspend inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        authCookie: String? = null
    ): ApiResult<T> = safeApiCall {
        httpClient.get(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            params.forEach { (key, value) ->
                parameter(key, value)
            }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, it)
            }
        }.body()
    }
    
    /**
     * POST请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param authCookie 认证Cookie（可选）
     */
    suspend inline fun <reified T> post(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        authCookie: String? = null
    ): ApiResult<T> = safeApiCall {
        httpClient.post(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let { setBody(it) }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, it)
            }
        }.body()
    }
    
    /**
     * PUT请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param authCookie 认证Cookie（可选）
     */
    suspend inline fun <reified T> put(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        authCookie: String? = null
    ): ApiResult<T> = safeApiCall {
        httpClient.put(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let { setBody(it) }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, it)
            }
        }.body()
    }
    
    /**
     * DELETE请求
     * @param url 请求URL
     * @param headers 请求头
     * @param authCookie 认证Cookie（可选）
     */
    suspend inline fun <reified T> delete(
        url: String,
        headers: Map<String, String> = emptyMap(),
        authCookie: String? = null
    ): ApiResult<T> = safeApiCall {
        httpClient.delete(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, it)
            }
        }.body()
    }
    
    /**
     * 关闭客户端
     */
    fun close() {
        httpClient.close()
    }
}


/**
 * 网络异常处理扩展
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): ApiResult<T> = try {
    val result = apiCall()
    ApiResult.Success(result)
} catch (e: ClientRequestException) {
    // 处理HTTP错误状态码
    val statusCode = e.response.status.value
    val errorMessage = when (statusCode) {
        401 -> "认证失败，请检查登录状态或服务器配置"
        403 -> "权限不足，无法访问该资源"
        404 -> "请求的资源不存在"
        500 -> "服务器内部错误，请稍后重试"
        else -> "HTTP错误: $statusCode - ${e.response.status.description}"
    }
    
    ApiResult.Error(
        exception = e,
        message = errorMessage,
        code = statusCode
    )
} catch (e: HttpRequestTimeoutException) {
    ApiResult.Error(
        exception = e,
        message = "请求超时,请检查网络连接",
        code = -1
    )
} catch (e: Exception) {
    ApiResult.Error(
        exception = e,
        message = e.message ?: "网络请求失败",
        code = -1
    )
}

/**
 * 扩展函数，用于处理ApiResult
 */
inline fun <T> ApiResult<T>.onSuccess(
    action: (value: T) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> ApiResult<T>.onError(
    action: (exception: Throwable?, message: String?, code: Int?) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Error) {
        action(exception, message, code)
    }
    return this
}

inline fun <T> ApiResult<T>.fold(
    onSuccess: (value: T) -> Unit,
    onError: (exception: Throwable?, message: String?, code: Int?) -> Unit
): ApiResult<T> {
    return onSuccess(onSuccess).onError(onError)
}