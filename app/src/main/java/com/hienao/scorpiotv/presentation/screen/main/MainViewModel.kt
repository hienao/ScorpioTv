package com.hienao.scorpiotv.presentation.screen.main

import com.hienao.scorpiotv.domain.model.NavigationItem
import com.hienao.scorpiotv.domain.model.NavigationItems
import com.hienao.scorpiotv.domain.model.User
import com.hienao.scorpiotv.domain.repository.UserRepository
import com.hienao.scorpiotv.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * 主界面的ViewModel
 * 处理主界面的业务逻辑
 */
class MainViewModel(
    private val userRepository: UserRepository
) : BaseViewModel<MainContract.UiState, MainContract.UiIntent, MainContract.UiEffect>(
    initialState = MainContract.UiState(
        navigationItems = NavigationItems.getAllItems()
    )
) {

    override fun handleIntent(intent: MainContract.UiIntent) {
        when (intent) {
            is MainContract.UiIntent.LoadUserData -> loadUserData()
            is MainContract.UiIntent.SelectNavigation -> selectNavigation(intent.navigationId)
            is MainContract.UiIntent.UpdateNavigationItems -> updateNavigationItems(intent.items)
            is MainContract.UiIntent.ShowLoginDialog -> showLoginDialog()
            is MainContract.UiIntent.HideLoginDialog -> hideLoginDialog()
            is MainContract.UiIntent.Logout -> logout()
            is MainContract.UiIntent.ClearError -> clearError()
        }
    }

    /**
     * 加载用户数据
     */
    private fun loadUserData() {
        launchViewModelScope {
            userRepository.getCurrentUser()
                .catch { exception ->
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "获取用户信息失败"
                        )
                    }
                    sendEffect(MainContract.UiEffect.ShowError(exception.message ?: "获取用户信息失败"))
                }
                .collect { user ->
                    updateState {
                        copy(
                            currentUser = user,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    
                    // 如果用户未登录，显示登录弹窗
                    if (user == null || !user.isLoggedIn) {
                        sendEffect(MainContract.UiEffect.LoginRequired)
                        showLoginDialog()
                    }
                }
        }
    }

    /**
     * 选择导航项
     */
    private fun selectNavigation(navigationId: String) {
        // 检查是否需要登录
        if (navigationId == "settings") {
            val currentUser = currentState.currentUser
            if (currentUser == null || !currentUser.isLoggedIn) {
                sendEffect(MainContract.UiEffect.LoginRequired)
                showLoginDialog()
                return
            }
        }

        updateState {
            copy(
                selectedNavigationId = navigationId,
                navigationItems = navigationItems.map { item ->
                    item.copy(isSelected = item.id == navigationId)
                }
            )
        }
    }

    /**
     * 更新导航项列表
     */
    private fun updateNavigationItems(items: List<NavigationItem>) {
        updateState {
            copy(
                navigationItems = items.map { item ->
                    item.copy(isSelected = item.id == currentState.selectedNavigationId)
                }
            )
        }
    }

    /**
     * 显示登录弹窗
     */
    private fun showLoginDialog() {
        updateState {
            copy(showLoginDialog = true)
        }
    }

    /**
     * 隐藏登录弹窗
     */
    private fun hideLoginDialog() {
        updateState {
            copy(showLoginDialog = false)
        }
    }

    /**
     * 登出
     */
    private fun logout() {
        val currentUser = currentState.currentUser
        if (currentUser == null) {
            sendEffect(MainContract.UiEffect.ShowError("用户未登录"))
            return
        }

        updateState {
            copy(isLoading = true)
        }

        launchViewModelScope {
            userRepository.logout(currentUser.serverUrl)
                .onSuccess {
                    // 登出成功，重新加载用户数据
                    loadUserData()
                    sendEffect(MainContract.UiEffect.ShowToast("登出成功"))
                }
                .onFailure { exception ->
                    updateState {
                        copy(isLoading = false)
                    }
                    sendEffect(MainContract.UiEffect.ShowError(exception.message ?: "登出失败"))
                }
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
     * 登录成功后的回调
     */
    fun onLoginSuccess() {
        hideLoginDialog()
        loadUserData()
        sendEffect(MainContract.UiEffect.ShowToast("登录成功"))
    }

    /**
     * 登录失败后的回调
     */
    fun onLoginFailed(errorMessage: String) {
        hideLoginDialog()
        updateState {
            copy(errorMessage = errorMessage)
        }
        sendEffect(MainContract.UiEffect.ShowError(errorMessage))
    }

    /**
     * 初始化
     */
    fun initialize() {
        loadUserData()
    }

    /**
     * 获取当前选中的导航项
     */
    fun getSelectedNavigationItem(): NavigationItem? {
        return currentState.navigationItems.find { it.isSelected }
    }

    /**
     * 检查用户是否已登录
     */
    fun isUserLoggedIn(): Boolean {
        return currentState.currentUser?.isLoggedIn == true
    }

    /**
     * 获取当前用户信息
     */
    fun getCurrentUser(): User? {
        return currentState.currentUser
    }
}