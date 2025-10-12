package com.hienao.scorpiotv.presentation.screen.search

import com.hienao.scorpiotv.domain.model.DoubanItem

/**
 * 搜索页面Contract
 */
interface SearchContract {
    
    data class UiState(
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val searchResults: List<DoubanItem> = emptyList(),
        val searchHistory: List<String> = emptyList(),
        val error: String? = null,
        val hasSearched: Boolean = false,
        val currentPage: Int = 1
    )
    
    sealed class UiIntent {
        data class UpdateSearchQuery(val searchQuery: String) : UiIntent()
        object PerformSearch : UiIntent()
        data class SelectSearchResult(val item: DoubanItem) : UiIntent()
        data class SelectHistoryItem(val searchQuery: String) : UiIntent()
        object ClearSearchHistory : UiIntent()
        object ClearError : UiIntent()
        object Refresh : UiIntent()
        object LoadMore : UiIntent()
    }
    
    sealed class UiEffect {
        data class NavigateToPlayer(val item: DoubanItem) : UiEffect()
        data class NavigateToDetail(val itemId: String) : UiEffect()
        data class ShowToast(val message: String) : UiEffect()
    }
}