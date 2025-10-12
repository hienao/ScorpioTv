package com.hienao.scorpiotv.presentation.screen.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienao.scorpiotv.domain.model.LiveChannel
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 直播页面ViewModel
 */
class LiveViewModel(
    private val doubanRepository: DoubanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LiveContract.UiState())
    val uiState: StateFlow<LiveContract.UiState> = _uiState.asStateFlow()
    
    private val _uiEffect = MutableStateFlow<LiveContract.UiEffect?>(null)
    val uiEffect: StateFlow<LiveContract.UiEffect?> = _uiEffect.asStateFlow()
    
    fun handleIntent(intent: LiveContract.UiIntent) {
        when (intent) {
            is LiveContract.UiIntent.LoadChannels -> loadChannels()
            is LiveContract.UiIntent.RefreshChannels -> refreshChannels()
            is LiveContract.UiIntent.SelectCategory -> selectCategory(intent.category)
            is LiveContract.UiIntent.PlayChannel -> playChannel(intent.channel)
        }
    }
    
    private fun loadChannels() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                // 模拟加载直播频道数据
                val channels = getMockChannels()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    channels = channels,
                    filteredChannels = channels
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载失败: ${e.message}"
                )
            }
        }
    }
    
    private fun refreshChannels() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadChannels()
    }
    
    private fun selectCategory(category: String) {
        val allChannels = _uiState.value.channels
        val filtered = if (category == "热门") {
            allChannels
        } else {
            allChannels.filter { it.category == category }
        }
        
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredChannels = filtered
        )
    }
    
    private fun playChannel(channel: LiveChannel) {
        _uiEffect.value = LiveContract.UiEffect.NavigateToPlayer(channel)
    }
    
    private fun getMockChannels(): List<LiveChannel> {
        return listOf(
            LiveChannel(
                id = "1",
                name = "央视综合",
                logo = "https://example.com/logo1.png",
                desc = "中央电视台综合频道",
                url = "https://example.com/live1",
                type = "live",
                rate = "9.5",
                status = "online",
                viewers = "10000+",
                language = "中文",
                country = "中国",
                category = "热门"
            ),
            LiveChannel(
                id = "2",
                name = "湖南卫视",
                logo = "https://example.com/logo2.png",
                desc = "湖南卫视直播",
                url = "https://example.com/live2",
                type = "live",
                rate = "9.2",
                status = "online",
                viewers = "8000+",
                language = "中文",
                country = "中国",
                category = "娱乐"
            ),
            LiveChannel(
                id = "3",
                name = "ESPN体育",
                logo = "https://example.com/logo3.png",
                desc = "ESPN体育频道",
                url = "https://example.com/live3",
                type = "live",
                rate = "8.8",
                status = "online",
                viewers = "5000+",
                language = "英文",
                country = "美国",
                category = "体育"
            )
        )
    }
}