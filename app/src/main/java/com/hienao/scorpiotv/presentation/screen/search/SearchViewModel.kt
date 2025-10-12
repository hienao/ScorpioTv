package com.hienao.scorpiotv.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 搜索页面的ViewModel
 */
class SearchViewModel(
    private val doubanRepository: DoubanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchContract.UiState())
    val uiState: StateFlow<SearchContract.UiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SearchContract.UiEffect>()
    val uiEffect: SharedFlow<SearchContract.UiEffect> = _uiEffect.asSharedFlow()

    /**
     * 处理用户意图
     */
    fun handleIntent(intent: SearchContract.UiIntent) {
        when (intent) {
            is SearchContract.UiIntent.UpdateSearchQuery -> updateSearchQuery(intent.searchQuery)
            is SearchContract.UiIntent.PerformSearch -> performSearch()
            is SearchContract.UiIntent.SelectSearchResult -> selectSearchResult(intent.item)
            is SearchContract.UiIntent.SelectHistoryItem -> selectHistoryItem(intent.searchQuery)
            is SearchContract.UiIntent.ClearSearchHistory -> clearSearchHistory()
            is SearchContract.UiIntent.ClearError -> clearError()
            is SearchContract.UiIntent.Refresh -> refresh()
            is SearchContract.UiIntent.LoadMore -> loadMore()
        }
    }

    /**
     * 更新搜索查询
     */
    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * 执行搜索
     */
    private fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            _uiState.update { it.copy(isLoading = false, searchResults = emptyList(), hasSearched = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }

            try {
                val result = doubanRepository.searchMedia(query)
                result.onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = items,
                            currentPage = 1
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "搜索失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "搜索失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 选择搜索结果
     */
    private fun selectSearchResult(item: DoubanItem) {
        viewModelScope.launch {
            _uiEffect.emit(SearchContract.UiEffect.NavigateToPlayer(item))
        }
    }

    /**
     * 选择历史记录项
     */
    private fun selectHistoryItem(searchQuery: String) {
        _uiState.update { it.copy(searchQuery = searchQuery) }
        performSearch()
    }

    /**
     * 清除搜索历史
     */
    private fun clearSearchHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }

    /**
     * 清除错误
     */
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 刷新搜索结果
     */
    private fun refresh() {
        performSearch()
    }

    /**
     * 加载更多结果
     */
    private fun loadMore() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.searchQuery.isBlank()) return

        val nextPage = currentState.currentPage + 1

        viewModelScope.launch {
            try {
                val result = doubanRepository.searchMedia(currentState.searchQuery)
                result.onSuccess { newItems ->
                    _uiState.update {
                        it.copy(
                            searchResults = it.searchResults + newItems,
                            currentPage = nextPage
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message ?: "加载更多失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "加载更多失败: ${e.message}"
                    )
                }
            }
        }
    }
}