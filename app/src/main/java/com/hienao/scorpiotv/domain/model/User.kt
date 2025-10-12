package com.hienao.scorpiotv.domain.model

/**
 * 用户领域模型
 */
data class User(
    val id: String,
    val username: String,
    val role: UserRole = UserRole.USER,
    val isLoggedIn: Boolean = false,
    val serverUrl: String = "",
    val loginTime: Long = 0L
)

/**
 * 用户角色枚举
 */
enum class UserRole(val displayName: String) {
    OWNER("站长"),
    ADMIN("管理员"),
    USER("普通用户")
}

/**
 * 登录请求模型
 */
data class LoginRequest(
    val serverUrl: String,
    val username: String? = null,
    val password: String
)

/**
 * 登录响应模型
 */
data class LoginResponse(
    val ok: Boolean,
    val error: String? = null
)

/**
 * 服务器配置模型
 */
data class ServerConfig(
    val url: String,
    val siteName: String = "",
    val storageType: String = "",
    val version: String = ""
)