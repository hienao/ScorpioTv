package com.hienao.scorpiotv.presentation.screen.home

import com.hienao.scorpiotv.domain.model.MediaItem
import com.hienao.scorpiotv.presentation.base.BaseContract

/**
 * Home页面的MVI Contract
 * 定义了Home页面的状态、意图和效果
 */
interface HomeContract {
    
    /**
     * Home页面的UI状态
     */
    data class UiState(
        val isLoading: Boolean = false,
        val mediaItems: List<MediaItem> = emptyList(),
        val categories: List<String> = emptyList(),
        val selectedCategory: String? = null,
        val error: String? = null,
        val isRefreshing: Boolean = false
    ) : BaseContract.UiState
    
    /**
     * Home页面的用户意图
     */
    sealed class UiIntent : BaseContract.UiIntent {
        object LoadData : UiIntent()
        object RefreshData : UiIntent()
        data class SelectCategory(val category: String) : UiIntent()
        data class SelectMediaItem(val mediaItem: MediaItem) : UiIntent()
        data class SearchMedia(val query: String) : UiIntent()
        object ClearError : UiIntent()
    }
    
    /**
     * Home页面的UI效果（一次性事件）
     */
    sealed class UiEffect : BaseContract.UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class NavigateToDetail(val mediaItem: MediaItem) : UiEffect()
        data class NavigateToSearch(val query: String) : UiEffect()
        object ShowNetworkError : UiEffect()
    }
}