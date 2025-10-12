package com.hienao.scorpiotv.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienao.scorpiotv.domain.model.User
import com.hienao.scorpiotv.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 设置页面ViewModel
 */
class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsContract.UiState())
    val uiState: StateFlow<SettingsContract.UiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsContract.UiEffect>()
    val uiEffect: SharedFlow<SettingsContract.UiEffect> = _uiEffect.asSharedFlow()

    init {
        initialize()
    }

    /**
     * 初始化设置页面
     */
    fun initialize() {
        viewModelScope.launch {
            // 加载用户信息
            loadUserInfo()
            
            // 加载设置项
            loadSettings()
        }
    }

    /**
     * 处理UI意图
     */
    fun handleIntent(intent: SettingsContract.UiIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsContract.UiIntent.LoadUserInfo -> {
                    loadUserInfo()
                }
                is SettingsContract.UiIntent.UpdateUserInfo -> {
                    updateUserInfo(intent.user)
                }
                is SettingsContract.UiIntent.UpdateSetting -> {
                    updateSetting(intent.key, intent.value)
                }
                is SettingsContract.UiIntent.ToggleDarkMode -> {
                    toggleDarkMode()
                }
                is SettingsContract.UiIntent.ToggleNotifications -> {
                    toggleNotifications()
                }
                is SettingsContract.UiIntent.SetCacheSize -> {
                    setCacheSize(intent.size)
                }
                is SettingsContract.UiIntent.ClearCache -> {
                    clearCache()
                }
                is SettingsContract.UiIntent.ShowAbout -> {
                    showAbout()
                }
                is SettingsContract.UiIntent.ShowPrivacyPolicy -> {
                    showPrivacyPolicy()
                }
                is SettingsContract.UiIntent.ShowTermsOfService -> {
                    showTermsOfService()
                }
                is SettingsContract.UiIntent.Logout -> {
                    logout()
                }
                is SettingsContract.UiIntent.Refresh -> {
                    refresh()
                }
                is SettingsContract.UiIntent.ClearError -> {
                    clearError()
                }
            }
        }
    }

    /**
     * 加载用户信息
     */
    private suspend fun loadUserInfo() {
        try {
            _uiState.update { it.copy(isLoading = true) }
            
            // 这里应该从用户仓库获取用户信息
            // 由于我们还没有实现用户仓库，这里使用一个模拟的用户对象
            val user = User(
                id = "user_123",
                username = "用户名",
                role = com.hienao.scorpiotv.domain.model.UserRole.USER,
                isLoggedIn = true,
                serverUrl = "https://example.com",
                loginTime = System.currentTimeMillis()
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    currentUser = user,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "加载用户信息失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 加载设置项
     */
    private suspend fun loadSettings() {
        try {
            // 这里应该从本地存储或远程配置加载设置项
            // 由于我们还没有实现这些功能，这里使用模拟数据
            
            _uiState.update { 
                it.copy(
                    darkMode = false,
                    notificationsEnabled = true,
                    autoPlayEnabled = true,
                    hdPlaybackEnabled = true,
                    cacheSize = 500L * 1024 * 1024, // 500MB
                    version = "1.0.0",
                    buildNumber = "1"
                )
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    error = "加载设置失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新用户信息
     */
    private suspend fun updateUserInfo(user: User) {
        try {
            _uiState.update { it.copy(isLoading = true) }
            
            // 这里应该调用用户仓库更新用户信息
            // userRepository.updateUser(user)
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    currentUser = user,
                    error = null
                )
            }
            
            _uiEffect.emit(SettingsContract.UiEffect.ShowToast("用户信息已更新"))
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "更新用户信息失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新设置项
     */
    private suspend fun updateSetting(key: String, value: Any) {
        try {
            // 这里应该保存设置到本地存储
            when (key) {
                "darkMode" -> {
                    _uiState.update { it.copy(darkMode = value as Boolean) }
                }
                "notificationsEnabled" -> {
                    _uiState.update { it.copy(notificationsEnabled = value as Boolean) }
                }
                "autoPlayEnabled" -> {
                    _uiState.update { it.copy(autoPlayEnabled = value as Boolean) }
                }
                "hdPlaybackEnabled" -> {
                    _uiState.update { it.copy(hdPlaybackEnabled = value as Boolean) }
                }
                "cacheSize" -> {
                    _uiState.update { it.copy(cacheSize = value as Long) }
                }
            }
            
            _uiEffect.emit(SettingsContract.UiEffect.ShowToast("设置已保存"))
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    error = "保存设置失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 切换深色模式
     */
    private suspend fun toggleDarkMode() {
        val newValue = !_uiState.value.darkMode
        updateSetting("darkMode", newValue)
    }

    /**
     * 切换通知
     */
    private suspend fun toggleNotifications() {
        val newValue = !_uiState.value.notificationsEnabled
        updateSetting("notificationsEnabled", newValue)
    }

    /**
     * 设置缓存大小
     */
    private suspend fun setCacheSize(size: Long) {
        updateSetting("cacheSize", size)
    }

    /**
     * 清除缓存
     */
    private suspend fun clearCache() {
        try {
            _uiState.update { it.copy(isLoading = true) }
            
            // 这里应该实际清除缓存
            // 为了演示，我们只是更新UI状态
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    cacheSize = 0L
                )
            }
            
            _uiEffect.emit(SettingsContract.UiEffect.ShowToast("缓存已清除"))
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "清除缓存失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 显示关于页面
     */
    private suspend fun showAbout() {
        _uiEffect.emit(SettingsContract.UiEffect.NavigateToAbout())
    }

    /**
     * 显示隐私政策
     */
    private suspend fun showPrivacyPolicy() {
        _uiEffect.emit(SettingsContract.UiEffect.NavigateToPrivacyPolicy())
    }

    /**
     * 显示服务条款
     */
    private suspend fun showTermsOfService() {
        _uiEffect.emit(SettingsContract.UiEffect.NavigateToTermsOfService())
    }

    /**
     * 退出登录
     */
    private suspend fun logout() {
        try {
            _uiState.update { it.copy(isLoading = true) }
            
            // 这里应该调用用户仓库的退出登录方法
            // userRepository.logout()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    currentUser = null
                )
            }
            
            _uiEffect.emit(SettingsContract.UiEffect.NavigateToLogin())
            _uiEffect.emit(SettingsContract.UiEffect.ShowToast("已退出登录"))
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "退出登录失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 刷新数据
     */
    private suspend fun refresh() {
        loadUserInfo()
        loadSettings()
    }

    /**
     * 清除错误
     */
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}