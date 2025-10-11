package com.hienao.scorpiotv.presentation.screen.home

import androidx.lifecycle.viewModelScope
import com.hienao.scorpiotv.domain.repository.MediaRepository
import com.hienao.scorpiotv.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * Home页面的ViewModel
 * 实现MVI架构模式，处理Home页面的业务逻辑
 */
class HomeViewModel(
    private val mediaRepository: MediaRepository
) : BaseViewModel<HomeContract.UiState, HomeContract.UiIntent, HomeContract.UiEffect>(
    initialState = HomeContract.UiState()
) {
    
    init {
        // 初始化时加载数据
        handleIntent(HomeContract.UiIntent.LoadData)
    }
    
    override fun handleIntent(intent: HomeContract.UiIntent) {
        when (intent) {
            is HomeContract.UiIntent.LoadData -> loadData()
            is HomeContract.UiIntent.RefreshData -> refreshData()
            is HomeContract.UiIntent.SelectCategory -> selectCategory(intent.category)
            is HomeContract.UiIntent.SelectMediaItem -> selectMediaItem(intent.mediaItem)
            is HomeContract.UiIntent.SearchMedia -> searchMedia(intent.query)
            is HomeContract.UiIntent.ClearError -> clearError()
        }
    }
    
    private fun loadData() {
        launchViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            
            try {
                // 并行加载媒体项和分类
                val mediaResult = mediaRepository.getMediaItems()
                val categoriesResult = mediaRepository.getMediaCategories()
                
                if (mediaResult.isSuccess && categoriesResult.isSuccess) {
                    val mediaItems = mediaResult.getOrNull() ?: emptyList()
                    val categories = categoriesResult.getOrNull()?.map { it.name } ?: emptyList()
                    
                    updateState {
                        copy(
                            isLoading = false,
                            mediaItems = mediaItems,
                            categories = categories,
                            error = null
                        )
                    }
                } else {
                    val error = mediaResult.exceptionOrNull()?.message 
                        ?: categoriesResult.exceptionOrNull()?.message 
                        ?: "加载数据失败"
                    
                    updateState {
                        copy(
                            isLoading = false,
                            error = error
                        )
                    }
                    
                    sendEffect(HomeContract.UiEffect.ShowNetworkError)
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "未知错误"
                    )
                }
                sendEffect(HomeContract.UiEffect.ShowToast("加载失败: ${e.message}"))
            }
        }
    }
    
    private fun refreshData() {
        launchViewModelScope {
            updateState { copy(isRefreshing = true, error = null) }
            
            try {
                val result = mediaRepository.getMediaItems()
                
                if (result.isSuccess) {
                    val mediaItems = result.getOrNull() ?: emptyList()
                    updateState {
                        copy(
                            isRefreshing = false,
                            mediaItems = mediaItems,
                            error = null
                        )
                    }
                    sendEffect(HomeContract.UiEffect.ShowToast("刷新成功"))
                } else {
                    updateState {
                        copy(
                            isRefreshing = false,
                            error = result.exceptionOrNull()?.message ?: "刷新失败"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isRefreshing = false,
                        error = e.message ?: "刷新失败"
                    )
                }
                sendEffect(HomeContract.UiEffect.ShowToast("刷新失败: ${e.message}"))
            }
        }
    }
    
    private fun selectCategory(category: String) {
        launchViewModelScope {
            updateState { copy(selectedCategory = category, isLoading = true) }
            
            try {
                val result = mediaRepository.getMediaItems(category = category)
                
                if (result.isSuccess) {
                    val mediaItems = result.getOrNull() ?: emptyList()
                    updateState {
                        copy(
                            isLoading = false,
                            mediaItems = mediaItems,
                            error = null
                        )
                    }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "加载分类失败"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "加载分类失败"
                    )
                }
            }
        }
    }
    
    private fun selectMediaItem(mediaItem: com.hienao.scorpiotv.domain.model.MediaItem) {
        sendEffect(HomeContract.UiEffect.NavigateToDetail(mediaItem))
    }
    
    private fun searchMedia(query: String) {
        if (query.isBlank()) {
            sendEffect(HomeContract.UiEffect.ShowToast("请输入搜索关键词"))
            return
        }
        
        sendEffect(HomeContract.UiEffect.NavigateToSearch(query))
    }
    
    private fun clearError() {
        updateState { copy(error = null) }
    }
}