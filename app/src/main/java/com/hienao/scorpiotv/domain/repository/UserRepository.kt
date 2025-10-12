package com.hienao.scorpiotv.domain.repository

import com.hienao.scorpiotv.domain.model.LoginRequest
import com.hienao.scorpiotv.domain.model.LoginResponse
import com.hienao.scorpiotv.domain.model.ServerConfig
import com.hienao.scorpiotv.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 用户仓库接口
 * 定义用户数据访问的抽象接口
 */
interface UserRepository {
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录结果
     */
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    
    /**
     * 用户登出
     * @param serverUrl 服务器地址
     * @return 登出结果
     */
    suspend fun logout(serverUrl: String): Result<Unit>
    
    /**
     * 获取当前用户信息
     * @return 用户信息Flow
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * 保存用户信息
     * @param user 用户信息
     * @return 保存结果
     */
    suspend fun saveUser(user: User): Result<Unit>
    
    /**
     * 清除用户信息
     * @return 清除结果
     */
    suspend fun clearUser(): Result<Unit>
    
    /**
     * 检查登录状态
     * @return 是否已登录
     */
    suspend fun isLoggedIn(): Boolean
    
    /**
     * 获取服务器配置
     * @param serverUrl 服务器地址
     * @return 服务器配置
     */
    suspend fun getServerConfig(serverUrl: String): Result<ServerConfig>
    
    /**
     * 保存服务器地址
     * @param serverUrl 服务器地址
     * @return 保存结果
     */
    suspend fun saveServerUrl(serverUrl: String): Result<Unit>
    
    /**
     * 获取保存的服务器地址
     * @return 服务器地址
     */
    suspend fun getServerUrl(): String?
    
    /**
     * 保存认证Cookie
     * @param authCookie 认证Cookie
     * @return 保存结果
     */
    suspend fun saveAuthCookie(authCookie: String): Result<Unit>
    
    /**
     * 获取认证Cookie
     * @return 认证Cookie
     */
    suspend fun getAuthCookie(): String?
}