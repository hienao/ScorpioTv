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
 * 提供统一的HTTP客户端实例��配置
 */
object NetworkClient {
    
    /**
     * 创建配置好的HttpClient实例
     * @param enableLogging 是否启用日志
     * @param timeout 超时时间(毫秒)
     */
    fun createHttpClient(
        enableLogging: Boolean = true,
        timeout: Long = 30_000L
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
     */
    suspend inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): ApiResult<T> = apiCall {
        httpClient.get(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            params.forEach { (key, value) ->
                parameter(key, value)
            }
        }.body()
    }
    
    /**
     * POST请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     */
    suspend inline fun <reified T> post(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T> = apiCall {
        httpClient.post(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let { setBody(it) }
        }.body()
    }
    
    /**
     * PUT请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     */
    suspend inline fun <reified T> put(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T> = apiCall {
        httpClient.put(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let { setBody(it) }
        }.body()
    }
    
    /**
     * DELETE请求
     * @param url 请求URL
     * @param headers 请求头
     */
    suspend inline fun <reified T> delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T> = apiCall {
        httpClient.delete(url) {
            headers.forEach { (key, value) ->
                header(key, value)
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
    ApiResult.Success(apiCall())
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