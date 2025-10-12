package com.hienao.scorpiotv.presentation.screen.settings

import com.hienao.scorpiotv.domain.model.User

/**
 * 设置页面Contract
 */
interface SettingsContract {

    /**
     * UI状态
     */
    data class UiState(
        val isLoading: Boolean = false,
        val currentUser: User? = null,
        val darkMode: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val autoPlayEnabled: Boolean = true,
        val hdPlaybackEnabled: Boolean = true,
        val cacheSize: Long = 0L,
        val version: String = "",
        val buildNumber: String = "",
        val error: String? = null
    )

    /**
     * UI意图
     */
    sealed class UiIntent {
        object LoadUserInfo : UiIntent()
        data class UpdateUserInfo(val user: User) : UiIntent()
        data class UpdateSetting(val key: String, val value: Any) : UiIntent()
        object ToggleDarkMode : UiIntent()
        object ToggleNotifications : UiIntent()
        data class SetCacheSize(val size: Long) : UiIntent()
        object ClearCache : UiIntent()
        object ShowAbout : UiIntent()
        object ShowPrivacyPolicy : UiIntent()
        object ShowTermsOfService : UiIntent()
        object Logout : UiIntent()
        object Refresh : UiIntent()
        object ClearError : UiIntent()
    }

    /**
     * UI效果
     */
    sealed class UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class NavigateToLogin(val dummy: Unit = Unit) : UiEffect()
        data class NavigateToAbout(val dummy: Unit = Unit) : UiEffect()
        data class NavigateToPrivacyPolicy(val dummy: Unit = Unit) : UiEffect()
        data class NavigateToTermsOfService(val dummy: Unit = Unit) : UiEffect()
    }
}