package com.hienao.scorpiotv.presentation.screen.main

import com.hienao.scorpiotv.domain.model.NavigationItem
import com.hienao.scorpiotv.domain.model.User
import com.hienao.scorpiotv.presentation.base.BaseContract

/**
 * 主界面的MVI Contract
 * 定义了主界面的状态、意图和效果
 */
interface MainContract {
    
    /**
     * 主界面的UI状态
     */
    data class UiState(
        val currentUser: User? = null,
        val navigationItems: List<NavigationItem> = emptyList(),
        val selectedNavigationId: String = "movie",
        val showLoginDialog: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : BaseContract.UiState
    
    /**
     * 主界面的用户意图
     */
    sealed class UiIntent : BaseContract.UiIntent {
        object LoadUserData : UiIntent()
        data class SelectNavigation(val navigationId: String) : UiIntent()
        data class UpdateNavigationItems(val items: List<NavigationItem>) : UiIntent()
        object ShowLoginDialog : UiIntent()
        object HideLoginDialog : UiIntent()
        object Logout : UiIntent()
        object ClearError : UiIntent()
    }
    
    /**
     * 主界面的UI效果（一次性事件）
     */
    sealed class UiEffect : BaseContract.UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class ShowError(val message: String) : UiEffect()
        object NavigateToLogin : UiEffect()
        object NavigateToSettings : UiEffect()
        object LoginRequired : UiEffect()
    }
}