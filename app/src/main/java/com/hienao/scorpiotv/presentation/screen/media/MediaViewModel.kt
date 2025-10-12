package com.hienao.scorpiotv.presentation.screen.media

import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.domain.model.DoubanCategories
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.MovieFilterState
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
            
            // 电影页面筛选相关Intent
            is MediaContract.UiIntent.SetPrimaryCategory -> setPrimaryCategory(intent.category)
            is MediaContract.UiIntent.SetFilterType -> setFilterType(intent.filterType)
            is MediaContract.UiIntent.SetSelectedType -> setSelectedType(intent.type)
            is MediaContract.UiIntent.SetSelectedRegion -> setSelectedRegion(intent.region)
            is MediaContract.UiIntent.SetSelectedSort -> setSelectedSort(intent.sort)
            is MediaContract.UiIntent.LoadRecommendCategories -> loadRecommendCategories()
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
                errorMessage = null,
                // 如果是电影类型，初始化筛选状态
                movieFilterState = if (type == "movie") {
                    MovieFilterState()
                } else {
                    currentState.movieFilterState
                }
            )
        }
        
        // 如果是电影类型且选择了全部分类，加载推荐分类数据
        if (type == "movie") {
            loadRecommendCategories()
        }
        
        // 加载新类型的数据
        if (type == "movie") {
            loadDataWithMovieFilter(refresh = true)
        } else {
            loadData(refresh = true)
        }
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
                
                // 根据媒体类型和分类调用相应的API
                val result = when (currentState.mediaType) {
                    "movie" -> {
                        if (currentState.selectedCategory == "全部") {
                            // 使用推荐接口
                            doubanRepository.getDoubanRecommends(
                                kind = "movie",
                                limit = PAGE_SIZE,
                                start = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                            )
                        } else {
                            // 使用普通豆瓣接口
                            doubanRepository.getDoubanData(
                                type = "movie",
                                tag = currentState.selectedCategory,
                                pageSize = PAGE_SIZE,
                                pageStart = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                            )
                        }
                    }
                    "tv" -> {
                        doubanRepository.getDoubanData(
                            type = "tv",
                            tag = currentState.selectedCategory,
                            pageSize = PAGE_SIZE,
                            pageStart = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                        )
                    }
                    "anime" -> {
                        doubanRepository.getDoubanData(
                            type = "anime",
                            tag = currentState.selectedCategory,
                            pageSize = PAGE_SIZE,
                            pageStart = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                        )
                    }
                    "show" -> {
                        doubanRepository.getDoubanData(
                            type = "show",
                            tag = currentState.selectedCategory,
                            pageSize = PAGE_SIZE,
                            pageStart = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                        )
                    }
                    else -> null
                }
                
                result?.collect { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    items = if (refresh) apiResult.data else currentState.items + apiResult.data,
                                    currentPage = if (refresh) 1 else currentState.currentPage + 1,
                                    hasMore = apiResult.data.size == PAGE_SIZE,
                                    errorMessage = null
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = apiResult.message ?: "加载数据失败"
                                )
                            }
                            sendEffect(MediaContract.UiEffect.ShowError(apiResult.message ?: "加载数据失败"))
                        }
                        is ApiResult.Loading -> {
                            // 已经在上面设置了loading状态
                        }
                    }
                }
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
                // 根据媒体类型和分类调用相应的API
                val result = when (currentState.mediaType) {
                    "movie" -> {
                        if (currentState.selectedCategory == "全部") {
                            // 使用推荐接口
                            doubanRepository.getDoubanRecommends(
                                kind = "movie",
                                limit = PAGE_SIZE,
                                start = currentState.currentPage * PAGE_SIZE
                            )
                        } else {
                            // 使用普通豆瓣接口
                            doubanRepository.getDoubanData(
                                type = "movie",
                                tag = currentState.selectedCategory,
                                pageSize = PAGE_SIZE,
                                pageStart = currentState.currentPage * PAGE_SIZE
                            )
                        }
                    }
                    "tv" -> {
                        doubanRepository.getDoubanData(
                            type = "tv",
                            tag = currentState.selectedCategory,
                            pageSize = PAGE_SIZE,
                            pageStart = currentState.currentPage * PAGE_SIZE
                        )
                    }
                    "anime" -> {
                        doubanRepository.getDoubanData(
                            type = "anime",
                            tag = currentState.selectedCategory,
                            pageSize = PAGE_SIZE,
                            pageStart = currentState.currentPage * PAGE_SIZE
                        )
                    }
                    "show" -> {
                        doubanRepository.getDoubanData(
                            type = "show",
                            tag = currentState.selectedCategory,
                            pageSize = PAGE_SIZE,
                            pageStart = currentState.currentPage * PAGE_SIZE
                        )
                    }
                    else -> null
                }
                
                result?.collect { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            updateState {
                                copy(
                                    isLoadingMore = false,
                                    items = currentState.items + apiResult.data,
                                    currentPage = currentState.currentPage + 1,
                                    hasMore = apiResult.data.size == PAGE_SIZE
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            updateState {
                                copy(isLoadingMore = false)
                            }
                            sendEffect(MediaContract.UiEffect.ShowError(apiResult.message ?: "加载更多数据失败"))
                        }
                        is ApiResult.Loading -> {
                            // 已经在上面设置了loading状态
                        }
                    }
                }
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
                // 这里需要实现搜索功能，暂时使用推荐接口代替
                val result = doubanRepository.getDoubanRecommends(
                    kind = "movie",
                    limit = PAGE_SIZE,
                    start = 0,
                    label = query
                )
                
                result.collect { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            updateState {
                                copy(
                                    isLoading = false,
                                    items = apiResult.data,
                                    errorMessage = null
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            updateState {
                                copy(
                                    isLoading = false,
                                    errorMessage = apiResult.message ?: "搜索失败"
                                )
                            }
                            sendEffect(MediaContract.UiEffect.ShowError(apiResult.message ?: "搜索失败"))
                        }
                        is ApiResult.Loading -> {
                            // 已经在上面设置了loading状态
                        }
                    }
                }
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
                // 搜索播放源，暂时使用推荐接口代替
                val result = doubanRepository.getDoubanRecommends(
                    kind = "movie",
                    limit = PAGE_SIZE,
                    start = 0,
                    label = item.title
                )
                
                result.collect { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            if (apiResult.data.isNotEmpty()) {
                                // 找到播放源，跳转到播放器
                                sendEffect(MediaContract.UiEffect.NavigateToPlayer(item))
                            } else {
                                // 没找到播放源，显示详情
                                sendEffect(MediaContract.UiEffect.NavigateToDetail(item))
                            }
                        }
                        is ApiResult.Error -> {
                            // 搜索失败，显示详情
                            sendEffect(MediaContract.UiEffect.NavigateToDetail(item))
                        }
                        is ApiResult.Loading -> {
                            // 处理中
                        }
                    }
                }
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
    
    /**
     * 设置第一行分类
     */
    private fun setPrimaryCategory(category: String) {
        val currentFilterState = currentState.movieFilterState
        val newFilterState = currentFilterState.copy(
            primaryCategory = category,
            useRecommendApi = category == "全部"
        )
        
        updateState {
            copy(
                movieFilterState = newFilterState,
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        
        // 如果选择了全部，需要加载推荐分类数据
        if (category == "全部") {
            loadRecommendCategories()
        }
        
        // 重新加载数据
        loadDataWithMovieFilter(refresh = true)
    }
    
    /**
     * 设置第二行筛选类型
     */
    private fun setFilterType(filterType: String) {
        val currentFilterState = currentState.movieFilterState
        val newFilterState = currentFilterState.copy(filterType = filterType)
        
        updateState {
            copy(
                movieFilterState = newFilterState,
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        
        // 重新加载数据
        loadDataWithMovieFilter(refresh = true)
    }
    
    /**
     * 设置选中的类型
     */
    private fun setSelectedType(type: String) {
        val currentFilterState = currentState.movieFilterState
        val newFilterState = currentFilterState.copy(selectedType = type)
        
        updateState {
            copy(
                movieFilterState = newFilterState,
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        
        // 重新加载数据
        loadDataWithMovieFilter(refresh = true)
    }
    
    /**
     * 设置选中的地区
     */
    private fun setSelectedRegion(region: String) {
        val currentFilterState = currentState.movieFilterState
        val newFilterState = currentFilterState.copy(selectedRegion = region)
        
        updateState {
            copy(
                movieFilterState = newFilterState,
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        
        // 重新加载数据
        loadDataWithMovieFilter(refresh = true)
    }
    
    /**
     * 设置选中的排序
     */
    private fun setSelectedSort(sort: String) {
        val currentFilterState = currentState.movieFilterState
        val newFilterState = currentFilterState.copy(selectedSort = sort)
        
        updateState {
            copy(
                movieFilterState = newFilterState,
                items = emptyList(),
                currentPage = 0,
                hasMore = true,
                errorMessage = null
            )
        }
        
        // 重新加载数据
        loadDataWithMovieFilter(refresh = true)
    }
    
    /**
     * 加载推荐分类数据
     */
    private fun loadRecommendCategories() {
        launchViewModelScope {
            try {
                val result = doubanRepository.getDoubanRecommendCategories("movie")
                
                result.collect { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            val currentFilterState = currentState.movieFilterState
                            val newFilterState = currentFilterState.copy(
                                typeOptions = apiResult.data.types,
                                sortOptions = apiResult.data.sorts.map { sortItem -> sortItem.label }
                            )
                            
                            updateState {
                                copy(movieFilterState = newFilterState)
                            }
                        }
                        is ApiResult.Error -> {
                            // 加载失败，使用默认选项
                            val currentFilterState = currentState.movieFilterState
                            val newFilterState = currentFilterState.copy(
                                typeOptions = listOf("电影", "电视剧"),
                                sortOptions = listOf("推荐", "最新", "评分")
                            )
                            
                            updateState {
                                copy(movieFilterState = newFilterState)
                            }
                        }
                        is ApiResult.Loading -> {
                            // 处理中
                        }
                    }
                }
            } catch (e: Exception) {
                // 异常处理，使用默认选项
                val currentFilterState = currentState.movieFilterState
                val newFilterState = currentFilterState.copy(
                    typeOptions = listOf("电影", "电视剧"),
                    sortOptions = listOf("推荐", "最新", "评分")
                )
                
                updateState {
                    copy(movieFilterState = newFilterState)
                }
            }
        }
    }
    
    /**
     * 使用电影筛选条件加载数据
     */
    private fun loadDataWithMovieFilter(refresh: Boolean = false) {
        val currentState = currentState
        val filterState = currentState.movieFilterState
        
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
                
                val result = if (filterState.useRecommendApi) {
                    // 使用推荐接口
                    val params = filterState.getApiParams()
                    doubanRepository.getDoubanRecommends(
                        kind = params["kind"] ?: "movie",
                        limit = PAGE_SIZE,
                        start = if (refresh) 0 else currentState.currentPage * PAGE_SIZE,
                        category = params["category"]?.takeIf { it.isNotBlank() },
                        format = params["format"]?.takeIf { it.isNotBlank() },
                        region = params["region"]?.takeIf { it.isNotBlank() },
                        year = params["year"]?.takeIf { it.isNotBlank() },
                        platform = params["platform"]?.takeIf { it.isNotBlank() },
                        sort = params["sort"]?.takeIf { it.isNotBlank() },
                        label = params["label"]?.takeIf { it.isNotBlank() }
                    )
                } else {
                    // 使用普通豆瓣接口
                    val params = filterState.getApiParams()
                    doubanRepository.getDoubanData(
                        type = params["type"] ?: "movie",
                        tag = params["tag"] ?: "热门",
                        pageSize = PAGE_SIZE,
                        pageStart = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
                    )
                }
                
                result.collect { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    items = if (refresh) apiResult.data else currentState.items + apiResult.data,
                                    currentPage = if (refresh) 1 else currentState.currentPage + 1,
                                    hasMore = apiResult.data.size == PAGE_SIZE,
                                    errorMessage = null
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = apiResult.message ?: "加载数据失败"
                                )
                            }
                            sendEffect(MediaContract.UiEffect.ShowError(apiResult.message ?: "加载数据失败"))
                        }
                        is ApiResult.Loading -> {
                            // 已经在上面设置了loading状态
                        }
                    }
                }
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
}