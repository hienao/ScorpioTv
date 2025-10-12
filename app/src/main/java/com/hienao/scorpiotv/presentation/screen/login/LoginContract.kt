package com.hienao.scorpiotv.presentation.screen.login

import com.hienao.scorpiotv.presentation.base.BaseContract

/**
 * 登录页面的MVI Contract
 * 定义了登录页面的状态、意图和效果
 */
interface LoginContract {
    
    /**
     * 登录页面的UI状态
     */
    data class UiState(
        val serverUrl: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isPasswordVisible: Boolean = false,
        val errorMessage: String? = null,
        val isLoginSuccess: Boolean = false
    ) : BaseContract.UiState
    
    /**
     * 登录页面的用户意图
     */
    sealed class UiIntent : BaseContract.UiIntent {
        data class UpdateServerUrl(val url: String) : UiIntent()
        data class UpdateUsername(val username: String) : UiIntent()
        data class UpdatePassword(val password: String) : UiIntent()
        object TogglePasswordVisibility : UiIntent()
        object Login : UiIntent()
        object ClearError : UiIntent()
        object Dismiss : UiIntent()
    }
    
    /**
     * 登录页面的UI效果（一次性事件）
     */
    sealed class UiEffect : BaseContract.UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class ShowError(val message: String) : UiEffect()
        object LoginSuccess : UiEffect()
        object Dismiss : UiEffect()
    }
}