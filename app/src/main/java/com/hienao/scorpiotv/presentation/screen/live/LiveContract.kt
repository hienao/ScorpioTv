package com.hienao.scorpiotv.presentation.screen.live

import com.hienao.scorpiotv.domain.model.LiveChannel

/**
 * 直播页面Contract定义
 */
interface LiveContract {
    
    data class UiState(
        val isLoading: Boolean = false,
        val channels: List<LiveChannel> = emptyList(),
        val selectedCategory: String = "热门",
        val categories: List<String> = listOf("热门", "娱乐", "体育", "游戏", "新闻"),
        val filteredChannels: List<LiveChannel> = emptyList(),
        val isRefreshing: Boolean = false,
        val error: String? = null
    )
    
    sealed class UiIntent {
        object LoadChannels : UiIntent()
        object RefreshChannels : UiIntent()
        data class SelectCategory(val category: String) : UiIntent()
        data class PlayChannel(val channel: LiveChannel) : UiIntent()
    }
    
    sealed class UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class NavigateToPlayer(val channel: LiveChannel) : UiEffect()
    }
}