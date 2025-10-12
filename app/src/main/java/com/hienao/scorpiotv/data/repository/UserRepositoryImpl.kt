package com.hienao.scorpiotv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.domain.model.LoginRequest
import com.hienao.scorpiotv.domain.model.LoginResponse
import com.hienao.scorpiotv.domain.model.ServerConfig
import com.hienao.scorpiotv.domain.model.User
import com.hienao.scorpiotv.domain.model.UserRole
import com.hienao.scorpiotv.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 用户仓库实现类
 */
class UserRepositoryImpl(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val LOGIN_TIME_KEY = stringPreferencesKey("login_time")
        private val AUTH_COOKIE_KEY = stringPreferencesKey("auth_cookie")
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(dispatcher) {
        try {
            // 设置API服务的基础URL
            apiService.setBaseUrl(request.serverUrl)
            
            // 调用登录API
            val result = apiService.login(request.username, request.password)
            
            when (result.result) {
                is ApiResult.Success -> {
                    val loginResponse = LoginResponse(
                        ok = result.result.data.ok,
                        error = result.result.data.error
                    )
                    
                    if (loginResponse.ok) {
                        // 登录成功，保存用户信息
                        val user = User(
                            id = request.username ?: "local_user",
                            username = request.username ?: "本地用户",
                            role = UserRole.USER, // 默认角色，后续可以通过其他API获取
                            isLoggedIn = true,
                            serverUrl = request.serverUrl,
                            loginTime = System.currentTimeMillis()
                        )
                        saveUser(user)
                        saveServerUrl(request.serverUrl)
                        
                        // 保存认证Cookie
                        result.setCookieHeader?.let { cookieHeader ->
                            // 从Set-Cookie头中提取auth cookie
                            val authCookie = extractAuthCookie(cookieHeader)
                            if (authCookie != null) {
                                saveAuthCookie(authCookie)
                            }
                        }
                    }
                    
                    Result.success(loginResponse)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.result.message ?: "登录失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("登录中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(serverUrl: String): Result<Unit> = withContext(dispatcher) {
        try {
            // 设置API服务的基础URL
            apiService.setBaseUrl(serverUrl)
            
            // 调用登出API
            val result = apiService.logout()
            
            // 无论API调用是否成功，都清除本地用户信息
            clearUser()
            
            when (result) {
                is ApiResult.Success -> {
                    Result.success(Unit)
                }
                is ApiResult.Error -> {
                    // 即使API调用失败，也返回成功，因为本地已清除
                    Result.success(Unit)
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("登出中..."))
                }
            }
        } catch (e: Exception) {
            // 即使出现异常，也清除本地信息
            clearUser()
            Result.success(Unit)
        }
    }

    override fun getCurrentUser(): Flow<User?> = dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: false
        if (!isLoggedIn) {
            null
        } else {
            User(
                id = preferences[USER_ID_KEY] ?: "",
                username = preferences[USERNAME_KEY] ?: "",
                role = UserRole.valueOf(
                    preferences[USER_ROLE_KEY] ?: UserRole.USER.name
                ),
                isLoggedIn = true,
                serverUrl = preferences[SERVER_URL_KEY] ?: "",
                loginTime = preferences[LOGIN_TIME_KEY]?.toLongOrNull() ?: 0L
            )
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> = withContext(dispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = user.id
                preferences[USERNAME_KEY] = user.username
                preferences[USER_ROLE_KEY] = user.role.name
                preferences[IS_LOGGED_IN_KEY] = user.isLoggedIn
                preferences[SERVER_URL_KEY] = user.serverUrl
                preferences[LOGIN_TIME_KEY] = user.loginTime.toString()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearUser(): Result<Unit> = withContext(dispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return dataStore.data.first()[IS_LOGGED_IN_KEY] ?: false
    }

    override suspend fun getServerConfig(serverUrl: String): Result<ServerConfig> = withContext(dispatcher) {
        try {
            apiService.setBaseUrl(serverUrl)
            val result = apiService.getServerConfig()
            
            when (result) {
                is ApiResult.Success -> {
                    val config = ServerConfig(
                        url = serverUrl,
                        siteName = result.data.SiteName,
                        storageType = result.data.StorageType,
                        version = result.data.Version
                    )
                    Result.success(config)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.message ?: "获取服务器配置失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("获取配置中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveServerUrl(serverUrl: String): Result<Unit> = withContext(dispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[SERVER_URL_KEY] = serverUrl
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServerUrl(): String? {
        return dataStore.data.first()[SERVER_URL_KEY]
    }

    override suspend fun saveAuthCookie(authCookie: String): Result<Unit> = withContext(dispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[AUTH_COOKIE_KEY] = authCookie
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAuthCookie(): String? {
        return dataStore.data.first()[AUTH_COOKIE_KEY]
    }
    
    /**
     * 从Set-Cookie头中提取auth cookie
     */
    private fun extractAuthCookie(setCookieHeader: String): String? {
        // Set-Cookie头可能包含多个cookie，格式如：
        // auth={"username":"user","password":"pass","signature":"sig","timestamp":123}; Path=/; HttpOnly
        try {
            // 查找auth=部分
            val authStart = setCookieHeader.indexOf("auth=")
            if (authStart == -1) return null
            
            val authValueStart = authStart + 5 // "auth=".length
            val authEnd = setCookieHeader.indexOf(';', authValueStart)
            val authValue = if (authEnd == -1) {
                setCookieHeader.substring(authValueStart)
            } else {
                setCookieHeader.substring(authValueStart, authEnd)
            }
            
            return authValue
        } catch (e: Exception) {
            // 解析失败，返回null
            return null
        }
    }
}