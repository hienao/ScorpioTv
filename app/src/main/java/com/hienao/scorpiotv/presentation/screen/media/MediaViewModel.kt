package com.hienao.scorpiotv.presentation.screen.media

import com.hienao.scorpiotv.domain.model.DoubanCategories
import com.hienao.scorpiotv.domain.model.DoubanFilter
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import com.hienao.scorpiotv.domain.repository.UserRepository
import com.hienao.scorpiotv.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 媒体内容页面的ViewModel
 * 处理媒体内容页面的业务逻辑
 */
class MediaViewModel(
    private val doubanRepository: DoubanRepository,
    private val userRepository: UserRepository
) : BaseViewModel<MediaContract.UiState, MediaContract.UiIntent, MediaContract.UiEffect>(
    initialState = MediaContract.UiState()
) {

    companion object {
        private const val PAGE_SIZE = 16
    }

    override fun handleIntent(intent: MediaContract.UiIntent) {
        when (intent) {
            is MediaContract.UiIntent.SetMediaType -> setMediaType(intent.type)
            is MediaContract.UiIntent.LoadData -> loadData(intent.refresh)
            is MediaContract.UiIntent.LoadMoreData -> loadMoreData()
            is MediaContract.UiIntent.SelectCategory -> selectCategory(intent.category)
            is MediaContract.UiIntent.SearchMedia -> searchMedia(intent.query)
            is MediaContract.UiIntent.SelectMediaItem -> selectMediaItem(intent.item)
            is MediaContract.UiIntent.ClearError -> clearError()
            is MediaContract.UiIntent.Refresh -> refresh()
        }
    }

    /**
     * 设置媒体类型
     */
    private fun setMediaType(type: String) {
        updateState {
            copy(
                mediaType = type,
                categories = DoubanCategories.getCategoriesByType(type),
                selectedCategory = DoubanCategories.getDefaultCategory(type),
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        // 加载新类型的数据
        loadData(refresh = true)
    }

    /**
     * 加载数据
     */
    private fun loadData(refresh: Boolean = false) {
        val currentState = currentState
        
        if (refresh) {
            updateState {
                copy(
                    isLoading = true,
                    isRefreshing = true,
                    items = emptyList(),
                    currentPage = 0,
                    hasMore = true,
                    errorMessage = null
                )
            }
        } else {
            updateState {
                copy(isLoading = true, errorMessage = null)
            }
        }

        launchViewModelScope {
            try {
                // 检查用户是否已登录
                val currentUser = userRepository.getCurrentUser().first()
                // 这里可以添加登录检查逻辑

                val filter = DoubanFilter(
                    type = currentState.mediaType,
                    category = currentState.selectedCategory,
                    tag = currentState.selectedCategory,
                    limit = PAGE_SIZE,
                    start = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                )

                val result = doubanRepository.getDataByFilter(filter)
                
                result.fold(
                    onSuccess = { response ->
                        updateState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                items = if (refresh) response.items else currentState.items + response.items,
                                currentPage = if (refresh) 1 else currentState.currentPage + 1,
                                hasMore = response.hasMore,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        updateState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = exception.message ?: "加载数据失败"
                            )
                        }
                        sendEffect(MediaContract.UiEffect.ShowError(exception.message ?: "加载数据失败"))
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "加载数据失败"
                    )
                }
                sendEffect(MediaContract.UiEffect.ShowError(e.message ?: "加载数据失败"))
            }
        }
    }

    /**
     * 加载更多数据
     */
    private fun loadMoreData() {
        val currentState = currentState
        if (currentState.isLoadingMore || !currentState.hasMore) {
            return
        }

        updateState {
            copy(isLoadingMore = true)
        }

        launchViewModelScope {
            try {
                val filter = DoubanFilter(
                    type = currentState.mediaType,
                    category = currentState.selectedCategory,
                    tag = currentState.selectedCategory,
                    limit = PAGE_SIZE,
                    start = currentState.currentPage * PAGE_SIZE
                )

                val result = doubanRepository.getDataByFilter(filter)
                
                result.fold(
                    onSuccess = { response ->
                        updateState {
                            copy(
                                isLoadingMore = false,
                                items = currentState.items + response.items,
                                currentPage = currentState.currentPage + 1,
                                hasMore = response.hasMore
                            )
                        }
                    },
                    onFailure = { exception ->
                        updateState {
                            copy(isLoadingMore = false)
                        }
                        sendEffect(MediaContract.UiEffect.ShowError(exception.message ?: "加载更多数据失败"))
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(isLoadingMore = false)
                }
                sendEffect(MediaContract.UiEffect.ShowError(e.message ?: "加载更多数据失败"))
            }
        }
    }

    /**
     * 选择分类
     */
    private fun selectCategory(category: String) {
        updateState {
            copy(
                selectedCategory = category,
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        loadData(refresh = true)
    }

    /**
     * 搜索媒体
     */
    private fun searchMedia(query: String) {
        if (query.isBlank()) {
            sendEffect(MediaContract.UiEffect.ShowError("请输入搜索关键词"))
            return
        }

        updateState {
            copy(
                searchQuery = query,
                isLoading = true,
                errorMessage = null
            )
        }

        launchViewModelScope {
            try {
                val result = doubanRepository.searchMedia(query)
                
                result.fold(
                    onSuccess = { items ->
                        updateState {
                            copy(
                                isLoading = false,
                                items = items,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        updateState {
                            copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "搜索失败"
                            )
                        }
                        sendEffect(MediaContract.UiEffect.ShowError(exception.message ?: "搜索失败"))
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = e.message ?: "搜索失败"
                    )
                }
                sendEffect(MediaContract.UiEffect.ShowError(e.message ?: "搜索失败"))
            }
        }
    }

    /**
     * 选择媒体项
     */
    private fun selectMediaItem(item: DoubanItem) {
        launchViewModelScope {
            try {
                // 搜索播放源
                val result = doubanRepository.searchMedia(item.title)
                result.fold(
                    onSuccess = { searchResults ->
                        if (searchResults.isNotEmpty()) {
                            // 找到播放源，跳转到播放器
                            sendEffect(MediaContract.UiEffect.NavigateToPlayer(item))
                        } else {
                            // 没找到播放源，显示详情
                            sendEffect(MediaContract.UiEffect.NavigateToDetail(item))
                        }
                    },
                    onFailure = { exception ->
                        // 搜索失败，显示详情
                        sendEffect(MediaContract.UiEffect.NavigateToDetail(item))
                    }
                )
            } catch (e: Exception) {
                // 搜索失败，显示详情
                sendEffect(MediaContract.UiEffect.NavigateToDetail(item))
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
     * 刷新数据
     */
    private fun refresh() {
        loadData(refresh = true)
    }

    /**
     * 初始化
     */
    fun initialize(mediaType: String) {
        setMediaType(mediaType)
    }

    /**
     * 获取当前媒体类型的中文名称
     */
    fun getCurrentMediaTypeDisplayName(): String {
        return when (currentState.mediaType) {
            "movie" -> "电影"
            "tv" -> "剧集"
            "anime" -> "动漫"
            "show" -> "综艺"
            else -> "未知"
        }
    }
}