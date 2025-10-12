package com.hienao.scorpiotv.presentation.screen.media

import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanFilter
import com.hienao.scorpiotv.presentation.base.BaseContract

/**
 * 媒体内容页面的MVI Contract
 * 定义了媒体内容页面的状态、意图和效果
 */
interface MediaContract {
    
    /**
     * 媒体内容页面的UI状态
     */
    data class UiState(
        val mediaType: String = "movie", // movie, tv, anime, show
        val items: List<DoubanItem> = emptyList(),
        val categories: List<String> = emptyList(),
        val selectedCategory: String = "热门",
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val currentPage: Int = 0,
        val errorMessage: String? = null,
        val searchQuery: String = ""
    ) : BaseContract.UiState
    
    /**
     * 媒体内容页面的用户意图
     */
    sealed class UiIntent : BaseContract.UiIntent {
        data class SetMediaType(val type: String) : UiIntent()
        data class LoadData(val refresh: Boolean = false) : UiIntent()
        data class LoadMoreData(val dummy: Unit = Unit) : UiIntent()
        data class SelectCategory(val category: String) : UiIntent()
        data class SearchMedia(val query: String) : UiIntent()
        data class SelectMediaItem(val item: DoubanItem) : UiIntent()
        object ClearError : UiIntent()
        object Refresh : UiIntent()
    }
    
    /**
     * 媒体内容页面的UI效果（一次性事件）
     */
    sealed class UiEffect : BaseContract.UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class ShowError(val message: String) : UiEffect()
        data class NavigateToDetail(val item: DoubanItem) : UiEffect()
        data class NavigateToSearch(val query: String) : UiEffect()
        data class NavigateToPlayer(val item: DoubanItem) : UiEffect()
    }
}