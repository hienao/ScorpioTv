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
import android.util.Log

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
    
    companion object {
        // 是否显示完整Cookie内容（仅用于调试，生产环境应设为false）
        var showFullCookie: Boolean = false
    }
    
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
    ): ApiResultWithCookie<T> = safeApiCallWithCookie {
        // 记录请求详情
        logRequestDetails("GET", url, headers, params, authCookie)
        
        val response = httpClient.get(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            params.forEach { (key, value) ->
                parameter(key, value)
            }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, "$it")
            }
        }
        
        val body = response.body<T>()
        val setCookieHeader = response.headers["Set-Cookie"]
        
        // 记录响应详情
        logResponseDetails(response.status.value, setCookieHeader, url, headers, emptyMap(), authCookie, response.headers)
        
        ApiResultWithCookie(
            result = ApiResult.Success(body),
            setCookieHeader = setCookieHeader
        )
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
    ): ApiResultWithCookie<T> = safeApiCallWithCookie {
        // 记录请求详情
        logRequestDetails("POST", url, headers, emptyMap(), authCookie)
        if (body != null) {
            Log.d("NetworkRequest", "请求体: ${body.toString()}")
        }
        
        val response = httpClient.post(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let { setBody(it) }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, "auth=$it")
            }
        }
        
        val responseBody = response.body<T>()
        val setCookieHeader = response.headers["Set-Cookie"]
        
        // 记录响应详情
        logResponseDetails(response.status.value, setCookieHeader, url, headers, emptyMap(), authCookie, response.headers)
        
        ApiResultWithCookie(
            result = ApiResult.Success(responseBody),
            setCookieHeader = setCookieHeader
        )
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
        // 记录请求详情
        logRequestDetails("PUT", url, headers, emptyMap(), authCookie)
        if (body != null) {
            Log.d("NetworkRequest", "请求体: ${body.toString()}")
        }
        
        val response = httpClient.put(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let { setBody(it) }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, "auth=$it")
            }
        }
        
        // 记录响应详情
        logResponseDetails(response.status.value, response.headers["Set-Cookie"], url, headers, emptyMap(), authCookie, response.headers)
        
        response.body()
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
        // 记录请求详情
        logRequestDetails("DELETE", url, headers, emptyMap(), authCookie)
        
        val response = httpClient.delete(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            // 添加认证Cookie
            authCookie?.let {
                header(HttpHeaders.Cookie, "auth=$it")
            }
        }
        
        // 记录响应详情
        logResponseDetails(response.status.value, response.headers["Set-Cookie"], url, headers, emptyMap(), authCookie, response.headers)
        
        response.body()
    }
    
    /**
     * 关闭客户端
     */
    fun close() {
        httpClient.close()
    }
    
    /**
     * 记录请求详情
     */
    fun logRequestDetails(
        method: String,
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>,
        authCookie: String?
    ) {
        Log.d("NetworkRequest", "=== 请求详情 ===")
        Log.d("NetworkRequest", "方法: $method")
        Log.d("NetworkRequest", "URL: $url")
        
        // 记录查询参数
        if (params.isNotEmpty()) {
            Log.d("NetworkRequest", "查询参数:")
            params.forEach { (key, value) ->
                Log.d("NetworkRequest", "  $key = $value")
            }
        }
        
        // 记录请求头
        if (headers.isNotEmpty()) {
            Log.d("NetworkRequest", "请求头:")
            headers.forEach { (key, value) ->
                Log.d("NetworkRequest", "  $key: $value")
            }
        }
        
        // 记录认证Cookie状态
        if (authCookie != null) {
            Log.d("NetworkRequest", "认证Cookie: 已设置 (长度: ${authCookie.length})")
            // 根据配置决定是否显示完整Cookie内容
            val cookieContent = if (showFullCookie) {
                // 显示完整Cookie内容（仅用于调试）
                "auth=$authCookie"
            } else {
                // 掩码处理，保护敏感信息
                if (authCookie.length > 20) {
                    "auth=${authCookie.take(10)}...${authCookie.takeLast(10)}"
                } else {
                    "auth=***"
                }
            }
            Log.d("NetworkRequest", "Cookie内容: $cookieContent")
            Log.d("NetworkRequest", "实际发送的Cookie头: auth=<${if (showFullCookie) authCookie else "***"}>")
        } else {
            Log.w("NetworkRequest", "认证Cookie: 未设置")
        }
        
        Log.d("NetworkRequest", "================")
    }
    
    /**
     * 记录响应详情
     */
    fun logResponseDetails(
        statusCode: Int,
        setCookieHeader: String?,
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>,
        authCookie: String?,
        responseHeaders: Headers
    ) {
        Log.d("NetworkResponse", "=== 响应详情 ===")
        Log.d("NetworkResponse", "状态码: $statusCode")
        
        // 记录Set-Cookie头
        if (setCookieHeader != null) {
            Log.d("NetworkResponse", "Set-Cookie: $setCookieHeader")
        }
        
        // 如果是401错误，使用详细的调试工具
        if (statusCode == 401) {
            val responseHeadersMap = mutableMapOf<String, String>()
            responseHeaders.names().forEach { name ->
                val values = responseHeaders.getAll(name)
                responseHeadersMap[name] = values?.joinToString(", ") ?: ""
            }
            
            NetworkDebugLogger.log401ErrorDetails(
                url = url,
                method = "GET",
                headers = headers,
                params = params,
                authCookie = authCookie,
                responseHeaders = responseHeadersMap,
                statusCode = statusCode
            )
        } else {
            Log.d("NetworkResponse", "================")
        }
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
 * 包含Cookie信息的API结果
 */
data class ApiResultWithCookie<T>(
    val result: ApiResult<T>,
    val setCookieHeader: String?
)

/**
 * 网络异常处理扩展（带Cookie）
 */
suspend fun <T> safeApiCallWithCookie(
    apiCall: suspend () -> ApiResultWithCookie<T>
): ApiResultWithCookie<T> = try {
    apiCall()
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
    
    ApiResultWithCookie(
        result = ApiResult.Error(
            exception = e,
            message = errorMessage,
            code = statusCode
        ),
        setCookieHeader = null
    )
} catch (e: HttpRequestTimeoutException) {
    ApiResultWithCookie(
        result = ApiResult.Error(
            exception = e,
            message = "请求超时,请检查网络连接",
            code = -1
        ),
        setCookieHeader = null
    )
} catch (e: Exception) {
    // 检查是否可能是401相关的异常
    val errorMessage = e.message ?: "网络请求失败"
    val isLikely401 = errorMessage.contains("401", ignoreCase = true) || 
                       errorMessage.contains("unauthorized", ignoreCase = true) ||
                       errorMessage.contains("认证", ignoreCase = true)
    
    // 如果疑似401错误，使用401状态码
    val errorCode = if (isLikely401) 401 else -1
    
    ApiResultWithCookie(
        result = ApiResult.Error(
            exception = e,
            message = errorMessage,
            code = errorCode
        ),
        setCookieHeader = null
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