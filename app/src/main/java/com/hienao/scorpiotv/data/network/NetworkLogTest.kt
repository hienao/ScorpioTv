package com.hienao.scorpiotv.data.network

import android.util.Log

/**
 * 网络日志测试示例
 * 用于演示增强后的日志输出效果
 */
object NetworkLogTest {
    
    /**
     * 测试401错误日志输出
     * 这个方法展示了当遇到401错误时，系统会输出什么样的调试信息
     */
    fun test401ErrorLogging() {
        // 模拟API调用参数
        val url = "http://nas.128139.xyz:8899/api/douban"
        val method = "GET"
        val headers = mapOf(
            "User-Agent" to "ScorpioTv/1.0",
            "Accept" to "application/json"
        )
        val params = mapOf(
            "type" to "movie",
            "tag" to "热门",
            "pageSize" to "16",
            "pageStart" to "0"
        )
        val authCookie = "sessionid=abc123def456; userid=789; expires=Wed, 15 Oct 2025 12:00:00 GMT"
        val responseHeaders = mapOf(
            "Content-Type" to "text/plain;charset=UTF-8",
            "Date" to "Tue, 14 Oct 2025 11:33:28 GMT",
            "WWW-Authenticate" to "Basic realm=\"Restricted Area\""
        )
        
        // 调用调试日志工具
        NetworkDebugLogger.log401ErrorDetails(
            url = url,
            method = method,
            headers = headers,
            params = params,
            authCookie = authCookie,
            responseHeaders = responseHeaders,
            statusCode = 401
        )
    }
    
    /**
     * 测试显示完整Cookie的401错误日志输出
     * 用于调试时查看完整的Cookie内容
     */
    fun test401ErrorWithFullCookie() {
        // 启用完整Cookie显示（仅用于调试）
        NetworkDebugLogger.showFullCookie = true
        KtorClient.showFullCookie = true
        
        Log.d("NetworkLogTest", "🔧 启用完整Cookie显示模式")
        
        // 模拟API调用参数
        val url = "http://nas.128139.xyz:8899/api/douban"
        val method = "GET"
        val params = mapOf(
            "type" to "movie",
            "tag" to "热门",
            "pageSize" to "16",
            "pageStart" to "0"
        )
        val authCookie = "sessionid=abc123def456789ghijklmnopqrstuvwxyz0123456789; userid=789; expires=Wed, 15 Oct 2025 12:00:00 GMT; Path=/; HttpOnly; Secure"
        
        // 调用调试日志工具，这次会显示完整Cookie
        NetworkDebugLogger.log401ErrorDetails(
            url = url,
            method = method,
            params = params,
            authCookie = authCookie,
            statusCode = 401
        )
        
        // 恢复默认设置（掩码模式）
        NetworkDebugLogger.showFullCookie = false
        KtorClient.showFullCookie = false
        
        Log.d("NetworkLogTest", "🔒 恢复Cookie掩码模式")
    }
    
    /**
     * 测试无Cookie的401错误日志输出
     */
    fun test401ErrorWithoutCookie() {
        val url = "http://nas.128139.xyz:8899/api/douban"
        val params = mapOf(
            "type" to "movie",
            "tag" to "热门",
            "pageSize" to "16",
            "pageStart" to "0"
        )
        
        NetworkDebugLogger.log401ErrorDetails(
            url = url,
            method = "GET",
            params = params,
            authCookie = null, // 没有Cookie
            statusCode = 401
        )
    }
    
    /**
     * 测试成功请求的日志输出
     */
    fun testSuccessLogging() {
        val url = "http://nas.128139.xyz:8899/api/douban"
        val method = "GET"
        val statusCode = 200
        val dataCount = 16
        
        NetworkDebugLogger.logSuccessDetails(
            url = url,
            method = method,
            statusCode = statusCode,
            dataCount = dataCount
        )
    }
    
    /**
     * 测试Cookie状态变化日志
     */
    fun testCookieStatusChange() {
        val oldCookie = "sessionid=old123"
        val newCookie = "sessionid=new456; userid=789"
        
        NetworkDebugLogger.logCookieStatusChange(
            operation = "用户登录",
            oldCookie = oldCookie,
            newCookie = newCookie
        )
    }
}

/**
 * 使用示例：
 * 
 * 在遇到401错误时，你会看到类似以下的日志输出：
 * 
 * 🚨🚨🚨 401认证错误详细分析 🚨🚨🚨
 * 时间戳: 2025-10-14 11:48:35.123
 * ==========================================
 * 📤 请求信息:
 *   方法: GET
 *   URL: http://nas.128139.xyz:8899/api/douban
 *   查询参数:
 *     type = movie
 *     tag = 热门
 *     pageSize = 16
 *     pageStart = 0
 * 🍪 Cookie分析:
 *   状态: 已设置
 *   长度: 67
 *   内容预览: sessionid=a...Wed, 15 Oct 2025 12:00:00 GMT
 *   Cookie格式分析:
 *     包含字段: sessionid
 *     Cookie段数: 3
 *     包含过期时间: 是
 * 📥 响应信息:
 *   状态码: 401
 *   响应头:
 *     Content-Type: text/plain;charset=UTF-8
 *     Date: Tue, 14 Oct 2025 11:33:28 GMT
 *     WWW-Authenticate: Basic realm="Restricted Area"
 * 🔍 可能的原因分析:
 *   1. Cookie已过期
 *   2. Cookie格式不正确
 *   3. Cookie中的会话ID无效
 *   4. 服务器端认证配置变更
 *   5. 用户权限不足
 * 🛠️ 调试建议:
 *   1. 检查用户登录状态
 *   2. 验证服务器URL是否正确
 *   3. 在浏览器中测试相同的请求
 *   4. 检查Cookie是否包含必要的认证信息
 *   5. 尝试重新登录获取新的Cookie
 * ==========================================
 * 🚨🚨🚨 401错误分析结束 🚨🚨🚨
 * 
 * 这样的详细日志输出将帮助你快速定位401错误的原因！
 */