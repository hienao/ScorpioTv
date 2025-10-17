package com.hienao.scorpiotv.data.network

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * 网络请求调试日志工具类
 * 专门用于调试网络请求问题，特别是401认证错误
 */
object NetworkDebugLogger {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    // 是否显示完整Cookie内容（仅用于调试，生产环境应设为false）
    var showFullCookie: Boolean = false
    
    /**
     * 记录401错误的详细调试信息
     * @param url 请求URL
     * @param method 请求方法
     * @param headers 请求头
     * @param params 请求参数
     * @param authCookie 认证Cookie
     * @param responseHeaders 响应头
     * @param statusCode 状态码
     */
    fun log401ErrorDetails(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        authCookie: String? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        statusCode: Int = 401
    ) {
        val timestamp = dateFormat.format(Date())
        
        Log.e("NetworkDebug", "🚨🚨🚨 401认证错误详细分析 🚨🚨🚨")
        Log.e("NetworkDebug", "时间戳: $timestamp")
        Log.e("NetworkDebug", "==========================================")
        
        // 请求基本信息
        Log.e("NetworkDebug", "📤 请求信息:")
        Log.e("NetworkDebug", "  方法: $method")
        Log.e("NetworkDebug", "  URL: $url")
        
        // 查询参数
        if (params.isNotEmpty()) {
            Log.e("NetworkDebug", "  查询参数:")
            params.forEach { (key, value) ->
                Log.e("NetworkDebug", "    $key = $value")
            }
        }
        
        // 请求头
        if (headers.isNotEmpty()) {
            Log.e("NetworkDebug", "  请求头:")
            headers.forEach { (key, value) ->
                Log.e("NetworkDebug", "    $key: $value")
            }
        }
        
        // Cookie详细信息
        Log.e("NetworkDebug", "🍪 Cookie分析:")
        if (authCookie != null) {
            Log.e("NetworkDebug", "  状态: 已设置")
            Log.e("NetworkDebug", "  长度: ${authCookie.length}")
            Log.e("NetworkDebug", "  内容预览: ${maskCookie(authCookie)}")
            
            // 分析Cookie格式
            analyzeCookieFormat(authCookie)
        } else {
            Log.e("NetworkDebug", "  状态: ❌ 未设置Cookie")
            Log.e("NetworkDebug", "  建议: 这很可能是401错误的原因")
        }
        
        // 响应信息
        Log.e("NetworkDebug", "📥 响应信息:")
        Log.e("NetworkDebug", "  状态码: $statusCode")
        
        if (responseHeaders.isNotEmpty()) {
            Log.e("NetworkDebug", "  响应头:")
            responseHeaders.forEach { (key, value) ->
                Log.e("NetworkDebug", "    $key: $value")
            }
        }
        
        // 可能的原因分析
        Log.e("NetworkDebug", "🔍 可能的原因分析:")
        if (authCookie == null) {
            Log.e("NetworkDebug", "  ❌ 主要原因: 未设置认证Cookie")
            Log.e("NetworkDebug", "  💡 解决方案: 确保用户已登录并正确保存Cookie")
        } else {
            Log.e("NetworkDebug", "  1. Cookie已过期")
            Log.e("NetworkDebug", "  2. Cookie格式不正确")
            Log.e("NetworkDebug", "  3. Cookie中的会话ID无效")
            Log.e("NetworkDebug", "  4. 服务器端认证配置变更")
            Log.e("NetworkDebug", "  5. 用户权限不足")
        }
        
        // 调试建议
        Log.e("NetworkDebug", "🛠️ 调试建议:")
        Log.e("NetworkDebug", "  1. 检查用户登录状态")
        Log.e("NetworkDebug", "  2. 验证服务器URL是否正确")
        Log.e("NetworkDebug", "  3. 在浏览器中测试相同的请求")
        Log.e("NetworkDebug", "  4. 检查Cookie是否包含必要的认证信息")
        Log.e("NetworkDebug", "  5. 尝试重新登录获取新的Cookie")
        
        Log.e("NetworkDebug", "==========================================")
        Log.e("NetworkDebug", "🚨🚨🚨 401错误分析结束 🚨🚨🚨")
    }
    
    /**
     * 记录网络请求成功的信息
     */
    fun logSuccessDetails(
        url: String,
        method: String = "GET",
        statusCode: Int = 200,
        dataCount: Int = 0
    ) {
        Log.d("NetworkDebug", "✅ 网络请求成功")
        Log.d("NetworkDebug", "  方法: $method")
        Log.d("NetworkDebug", "  URL: $url")
        Log.d("NetworkDebug", "  状态码: $statusCode")
        Log.d("NetworkDebug", "  数据量: $dataCount 条")
    }
    
    /**
     * 记录图片加载错误的详细调试信息
     * @param url 图片URL
     * @param error 错误异常
     * @param requestHeaders 请求头
     */
    fun logImageLoadError(
        url: String,
        error: Throwable,
        requestHeaders: Map<String, String> = emptyMap()
    ) {
        val timestamp = dateFormat.format(Date())
        
        Log.e("ImageLoadDebug", "🖼️🖼️🖼️ 图片加载错误详细分析 🖼️🖼️🖼️")
        Log.e("ImageLoadDebug", "时间戳: $timestamp")
        Log.e("ImageLoadDebug", "==========================================")
        
        // 基本信息
        Log.e("ImageLoadDebug", "📤 图片请求信息:")
        Log.e("ImageLoadDebug", "  URL: $url")
        Log.e("ImageLoadDebug", "  错误类型: ${error.javaClass.simpleName}")
        Log.e("ImageLoadDebug", "  错误信息: ${error.message}")
        
        // 请求头
        if (requestHeaders.isNotEmpty()) {
            Log.e("ImageLoadDebug", "  请求头:")
            requestHeaders.forEach { (key, value) ->
                Log.e("ImageLoadDebug", "    $key: $value")
            }
        }
        
        // 错误原因分析
        Log.e("ImageLoadDebug", "🔍 错误原因分析:")
        when {
            error is java.net.UnknownHostException -> {
                Log.e("ImageLoadDebug", "  ❌ 主机名解析失败")
                Log.e("ImageLoadDebug", "  💡 可能原因: 域名不存在或DNS解析失败")
                Log.e("ImageLoadDebug", "  💡 解决方案: 检查URL是否正确，检查网络连接")
            }
            error is java.net.SocketTimeoutException -> {
                Log.e("ImageLoadDebug", "  ❌ 请求超时")
                Log.e("ImageLoadDebug", "  💡 可能原因: 网络慢或服务器响应慢")
                Log.e("ImageLoadDebug", "  💡 解决方案: 增加超时时间或使用CDN")
            }
            error is java.net.ConnectException -> {
                Log.e("ImageLoadDebug", "  ❌ 连接失败")
                Log.e("ImageLoadDebug", "  💡 可能原因: 服务器不可达或端口被阻止")
                Log.e("ImageLoadDebug", "  💡 解决方案: 检查服务器状态和网络配置")
            }
            error is java.io.IOException -> {
                Log.e("ImageLoadDebug", "  ❌ IO异常")
                Log.e("ImageLoadDebug", "  💡 可能原因: 网络中断或数据传输错误")
                Log.e("ImageLoadDebug", "  💡 解决方案: 检查网络连接稳定性")
            }
            error.message?.contains("404", ignoreCase = true) == true -> {
                Log.e("ImageLoadDebug", "  ❌ 图片不存在 (404)")
                Log.e("ImageLoadDebug", "  💡 可能原因: 图片已被删除或URL错误")
                Log.e("ImageLoadDebug", "  💡 解决方案: 检查图片URL是否正确")
            }
            error.message?.contains("403", ignoreCase = true) == true -> {
                Log.e("ImageLoadDebug", "  ❌ 访问被拒绝 (403)")
                Log.e("ImageLoadDebug", "  💡 可能原因: 需要认证或权限不足")
                Log.e("ImageLoadDebug", "  💡 解决方案: 添加认证信息或检查权限")
            }
            error.message?.contains("decoding", ignoreCase = true) == true -> {
                Log.e("ImageLoadDebug", "  ❌ 图片解码失败")
                Log.e("ImageLoadDebug", "  💡 可能原因: 图片格式不支持或文件损坏")
                Log.e("ImageLoadDebug", "  💡 解决方案: 检查图片格式，添加更多解码器")
            }
            else -> {
                Log.e("ImageLoadDebug", "  ❌ 未知错误")
                Log.e("ImageLoadDebug", "  💡 建议: 检查完整的错误堆栈信息")
            }
        }
        
        // URL分析
        Log.e("ImageLoadDebug", "🔗 URL分析:")
        analyzeImageUrl(url)
        
        // 调试建议
        Log.e("ImageLoadDebug", "🛠️ 调试建议:")
        Log.e("ImageLoadDebug", "  1. 在浏览器中直接访问该URL")
        Log.e("ImageLoadDebug", "  2. 检查图片是否存在且可访问")
        Log.e("ImageLoadDebug", "  3. 验证图片格式是否受支持")
        Log.e("ImageLoadDebug", "  4. 检查网络连接和防火墙设置")
        Log.e("ImageLoadDebug", "  5. 尝试使用其他图片URL测试")
        
        Log.e("ImageLoadDebug", "==========================================")
        Log.e("ImageLoadDebug", "🖼️🖼️🖼️ 图片加载错误分析结束 🖼️🖼️🖼️")
    }
    
    /**
     * 分析图片URL
     */
    fun analyzeImageUrl(url: String) {
        Log.e("ImageLoadDebug", "  URL格式分析:")
        
        // 检查协议
        when {
            url.startsWith("https://") -> Log.e("ImageLoadDebug", "    协议: HTTPS (安全)")
            url.startsWith("http://") -> Log.e("ImageLoadDebug", "    协议: HTTP (非安全)")
            else -> Log.e("ImageLoadDebug", "    ⚠️ 协议: 未知或无效")
        }
        
        // 检查域名
        try {
            val uri = java.net.URI(url)
            val host = uri.host
            if (host != null) {
                Log.e("ImageLoadDebug", "    域名: $host")
                
                // 检查是否是常见的图片托管服务
                val commonHosts = listOf("imgur.com", "i.imgur.com", "cdn.com", "cloudinary.com", "aws.amazon.com")
                val isCommonHost = commonHosts.any { host.contains(it) }
                Log.e("ImageLoadDebug", "    常见图片托管: ${if (isCommonHost) "是" else "否"}")
            } else {
                Log.e("ImageLoadDebug", "    ⚠️ 域名: 无法解析")
            }
        } catch (e: Exception) {
            Log.e("ImageLoadDebug", "    ⚠️ 域名解析失败: ${e.message}")
        }
        
        // 检查文件扩展名
        val imageExtensions = mapOf(
            ".jpg" to "JPEG",
            ".jpeg" to "JPEG",
            ".png" to "PNG",
            ".gif" to "GIF",
            ".webp" to "WebP",
            ".bmp" to "BMP",
            ".svg" to "SVG"
        )
        
        val hasValidExtension = imageExtensions.any { (ext, format) ->
            if (url.lowercase().endsWith(ext)) {
                Log.e("ImageLoadDebug", "    文件格式: $format ($ext)")
                true
            } else {
                false
            }
        }
        
        if (!hasValidExtension) {
            Log.e("ImageLoadDebug", "    ⚠️ 文件格式: 未知或无扩展名")
            Log.e("ImageLoadDebug", "    💡 可能是动态生成的图片URL")
        }
        
        // 检查URL长度
        Log.e("ImageLoadDebug", "    URL长度: ${url.length} 字符")
        if (url.length > 2048) {
            Log.e("ImageLoadDebug", "    ⚠️ URL过长，可能导致请求失败")
        }
    }
    
    /**
     * 分析Cookie格式
     */
    private fun analyzeCookieFormat(cookie: String) {
        Log.e("NetworkDebug", "  Cookie格式分析:")
        
        // 检查是否包含常见的Cookie字段
        val commonFields = listOf("sessionid", "token", "auth", "user", "sid", "jsessionid")
        val cookieLower = cookie.lowercase()
        
        var foundFields = mutableListOf<String>()
        commonFields.forEach { field ->
            if (field in cookieLower) {
                foundFields.add(field)
            }
        }
        
        if (foundFields.isNotEmpty()) {
            Log.e("NetworkDebug", "    包含字段: ${foundFields.joinToString(", ")}")
        } else {
            Log.e("NetworkDebug", "    ⚠️ 未发现常见认证字段")
        }
        
        // 检查Cookie格式
        val cookieParts = cookie.split(";")
        Log.e("NetworkDebug", "    Cookie段数: ${cookieParts.size}")
        
        if (cookieParts.size == 1) {
            Log.e("NetworkDebug", "    ⚠️ Cookie格式可能过于简单")
        }
        
        // 检查是否包含过期时间
        val hasExpires = cookieLower.contains("expires") || cookieLower.contains("max-age")
        Log.e("NetworkDebug", "    包含过期时间: ${if (hasExpires) "是" else "否"}")
    }
    
    /**
     * 掩码Cookie内容，根据配置决定是否显示完整内容
     */
    private fun maskCookie(cookie: String): String {
        return if (showFullCookie) {
            // 显示完整Cookie内容（仅用于调试）
            cookie
        } else {
            // 掩码处理，保护敏感信息
            if (cookie.length <= 20) {
                "***"
            } else {
                "${cookie.take(10)}***${cookie.takeLast(10)}"
            }
        }
    }
    
    /**
     * 记录Cookie状态变化
     */
    fun logCookieStatusChange(operation: String, oldCookie: String?, newCookie: String?) {
        Log.d("NetworkDebug", "🍪 Cookie状态变化: $operation")
        Log.d("NetworkDebug", "  旧Cookie: ${if (oldCookie != null) "存在 (长度: ${oldCookie.length})" else "不存在"}")
        Log.d("NetworkDebug", "  新Cookie: ${if (newCookie != null) "存在 (长度: ${newCookie.length})" else "不存在"}")
        
        if (oldCookie != null && newCookie != null) {
            Log.d("NetworkDebug", "  Cookie变化: ${if (oldCookie == newCookie) "无变化" else "已更新"}")
        }
    }
}