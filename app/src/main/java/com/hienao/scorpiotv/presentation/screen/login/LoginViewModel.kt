package com.hienao.scorpiotv.presentation.screen.login

import com.hienao.scorpiotv.domain.model.LoginRequest
import com.hienao.scorpiotv.domain.repository.UserRepository
import com.hienao.scorpiotv.presentation.base.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext.get

/**
 * 登录页面的ViewModel
 * 处理登录相关的业务逻辑
 */
class LoginViewModel(
    private val userRepository: UserRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel<LoginContract.UiState, LoginContract.UiIntent, LoginContract.UiEffect>(
    initialState = LoginContract.UiState()
) {

    override fun handleIntent(intent: LoginContract.UiIntent) {
        when (intent) {
            is LoginContract.UiIntent.UpdateServerUrl -> updateServerUrl(intent.url)
            is LoginContract.UiIntent.UpdateUsername -> updateUsername(intent.username)
            is LoginContract.UiIntent.UpdatePassword -> updatePassword(intent.password)
            is LoginContract.UiIntent.TogglePasswordVisibility -> togglePasswordVisibility()
            is LoginContract.UiIntent.Login -> login()
            is LoginContract.UiIntent.ClearError -> clearError()
            is LoginContract.UiIntent.Dismiss -> dismiss()
        }
    }

    /**
     * 更新服务器地址
     */
    private fun updateServerUrl(url: String) {
        updateState {
            copy(
                serverUrl = url,
                errorMessage = null
            )
        }
    }

    /**
     * 更新用户名
     */
    private fun updateUsername(username: String) {
        updateState {
            copy(
                username = username,
                errorMessage = null
            )
        }
    }

    /**
     * 更新密码
     */
    private fun updatePassword(password: String) {
        updateState {
            copy(
                password = password,
                errorMessage = null
            )
        }
    }

    /**
     * 切换密码可见性
     */
    private fun togglePasswordVisibility() {
        updateState {
            copy(isPasswordVisible = !isPasswordVisible)
        }
    }

    /**
     * 执行登录
     */
    private fun login() {
        val currentState = currentState
        
        // 验证输入
        if (!validateInput(currentState)) {
            return
        }

        // 设置加载状态
        updateState {
            copy(isLoading = true, errorMessage = null)
        }

        launchViewModelScope {
            try {
                val loginRequest = LoginRequest(
                    serverUrl = currentState.serverUrl.trim(),
                    username = currentState.username.trim().takeIf { it.isNotBlank() },
                    password = currentState.password
                )

                val result = withContext(dispatcher) {
                    userRepository.login(loginRequest)
                }

                updateState {
                    copy(isLoading = false)
                }

                result.fold(
                    onSuccess = { loginResponse ->
                        if (loginResponse.ok) {
                            updateState {
                                copy(isLoginSuccess = true)
                            }
                            sendEffect(LoginContract.UiEffect.LoginSuccess)
                            sendEffect(LoginContract.UiEffect.Dismiss)
                        } else {
                            val errorMsg = loginResponse.error ?: "登录失败"
                            updateState {
                                copy(errorMessage = errorMsg)
                            }
                            sendEffect(LoginContract.UiEffect.ShowError(errorMsg))
                        }
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "登录失败"
                        updateState {
                            copy(errorMessage = errorMsg)
                        }
                        sendEffect(LoginContract.UiEffect.ShowError(errorMsg))
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(isLoading = false, errorMessage = e.message ?: "登录失败")
                }
                sendEffect(LoginContract.UiEffect.ShowError(e.message ?: "登录失败"))
            }
        }
    }

    /**
     * 验证输入
     */
    private fun validateInput(state: LoginContract.UiState): Boolean {
        return when {
            state.serverUrl.isBlank() -> {
                updateState {
                    copy(errorMessage = "请输入服务器地址")
                }
                sendEffect(LoginContract.UiEffect.ShowError("请输入服务器地址"))
                false
            }
            !isValidUrl(state.serverUrl) -> {
                updateState {
                    copy(errorMessage = "请输入有效的服务器地址")
                }
                sendEffect(LoginContract.UiEffect.ShowError("请输入有效的服务器地址"))
                false
            }
            state.password.isBlank() -> {
                updateState {
                    copy(errorMessage = "请输入密码")
                }
                sendEffect(LoginContract.UiEffect.ShowError("请输入密码"))
                false
            }
            else -> true
        }
    }

    /**
     * 验证URL格式
     */
    private fun isValidUrl(url: String): Boolean {
        val trimmedUrl = url.trim()
        return try {
            // 简单的URL格式验证
            val httpUrl = if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                "http://$trimmedUrl"
            } else {
                trimmedUrl
            }
            java.net.URL(httpUrl)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 清除错误信息
     */
    private fun clearError() {
        updateState {
            copy(errorMessage = null)
        }
    }

    /**
     * 关闭登录弹窗
     */
    private fun dismiss() {
        sendEffect(LoginContract.UiEffect.Dismiss)
    }

    /**
     * 检查是否已登录
     */
    fun checkLoginStatus() {
        launchViewModelScope {
            try {
                val isLoggedIn = withContext(dispatcher) {
                    userRepository.isLoggedIn()
                }
                if (isLoggedIn) {
                    updateState {
                        copy(isLoginSuccess = true)
                    }
                    sendEffect(LoginContract.UiEffect.Dismiss)
                }
            } catch (e: Exception) {
                // 忽略检查登录状态的错误
            }
        }
    }

    /**
     * 获取保存的服务器地址
     */
    fun loadSavedServerUrl() {
        launchViewModelScope {
            try {
                val savedUrl = withContext(dispatcher) {
                    userRepository.getServerUrl()
                }
                if (savedUrl != null) {
                    updateState {
                        copy(serverUrl = savedUrl)
                    }
                }
            } catch (e: Exception) {
                // 忽略加载服务器地址的错误
            }
        }
    }
}