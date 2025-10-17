package com.hienao.scorpiotv.presentation.screen.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.hienao.scorpiotv.R
import com.hienao.scorpiotv.ui.image.CoilImageLoader
import kotlinx.coroutines.flow.distinctUntilChanged
import com.hienao.scorpiotv.presentation.base.CollectAsEffect
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel

/**
 * 媒体内容页面Screen
 */
@Composable
fun MediaScreen(
    mediaType: String,
    viewModel: MediaViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    // 处理UI效果
    viewModel.uiEffect.CollectAsEffect { effect ->
        when (effect) {
            is MediaContract.UiEffect.ShowToast -> {
                // 这里可以显示Toast
            }
            is MediaContract.UiEffect.ShowError -> {
                // 错误信息已经在UI状态中处理
            }
            is MediaContract.UiEffect.NavigateToDetail -> {
                // 导航到详情页面
            }
            is MediaContract.UiEffect.NavigateToSearch -> {
                // 导航到搜索页面
            }
            is MediaContract.UiEffect.NavigateToPlayer -> {
                // 导航到播放器页面
            }
        }
    }

    // 初始化
    LaunchedEffect(mediaType) {
        viewModel.initialize(mediaType)
    }

    // 加载更多
    LaunchedEffect(gridState.canScrollForward) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.items.size - 3 && !uiState.isLoadingMore && uiState.hasMore) {
                    viewModel.handleIntent(MediaContract.UiIntent.LoadMoreData(Unit))
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏和筛选
        if (uiState.mediaType == "movie") {
            // 电影页面使用新的筛选组件
            Column {
                MediaTopBar(
                    selectedCategory = uiState.selectedCategory,
                    categories = uiState.categories,
                    onCategorySelected = { category ->
                        viewModel.handleIntent(MediaContract.UiIntent.SelectCategory(category))
                    },
                    onRefresh = {
                        viewModel.handleIntent(MediaContract.UiIntent.Refresh)
                    },
                    isRefreshing = uiState.isRefreshing,
                    mediaTypeDisplayName = viewModel.getCurrentMediaTypeDisplayName()
                )
                
                // 电影页面筛选组件
                MovieFilterSection(
                    filterState = uiState.movieFilterState,
                    onPrimaryCategorySelected = { category ->
                        viewModel.handleIntent(MediaContract.UiIntent.SetPrimaryCategory(category))
                    },
                    onFilterTypeSelected = { filterType ->
                        viewModel.handleIntent(MediaContract.UiIntent.SetFilterType(filterType))
                    },
                    onTypeSelected = { type ->
                        viewModel.handleIntent(MediaContract.UiIntent.SetSelectedType(type))
                    },
                    onRegionSelected = { region ->
                        viewModel.handleIntent(MediaContract.UiIntent.SetSelectedRegion(region))
                    },
                    onSortSelected = { sort ->
                        viewModel.handleIntent(MediaContract.UiIntent.SetSelectedSort(sort))
                    }
                )
            }
        } else {
            // 其他媒体类型使用原有的筛选栏
            MediaTopBar(
                selectedCategory = uiState.selectedCategory,
                categories = uiState.categories,
                onCategorySelected = { category ->
                    viewModel.handleIntent(MediaContract.UiIntent.SelectCategory(category))
                },
                onRefresh = {
                    viewModel.handleIntent(MediaContract.UiIntent.Refresh)
                },
                isRefreshing = uiState.isRefreshing,
                mediaTypeDisplayName = viewModel.getCurrentMediaTypeDisplayName()
            )
        }

        // 内容区域
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                // 加载中
                LoadingIndicator()
            } else if (uiState.items.isEmpty() && !uiState.isLoading) {
                // 空状态
                EmptyState(
                    onRetry = {
                        viewModel.handleIntent(MediaContract.UiIntent.Refresh)
                    }
                )
            } else {
                // 媒体网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.items) { item ->
                        MediaItemCard(
                            item = item,
                            onClick = {
                                viewModel.handleIntent(MediaContract.UiIntent.SelectMediaItem(item))
                            }
                        )
                    }
                    
                    // 加载更多指示器
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            // 错误提示
            uiState.errorMessage?.let { error ->
                LaunchedEffect(error) {
                    // 这里可以显示Snackbar
                    viewModel.handleIntent(MediaContract.UiIntent.ClearError)
                }
            }
        }
    }
}

/**
 * 媒体顶部栏
 */
@Composable
private fun MediaTopBar(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    mediaTypeDisplayName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题和刷新按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mediaTypeDisplayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "刷新"
                    )
                }
            }
        }
    }
}

/**
 * 媒体项卡片
 */
@Composable
private fun MediaItemCard(
    item: com.hienao.scorpiotv.domain.model.DoubanItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 海报
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.poster)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build(),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    // 验证图片URL并记录错误
                    LaunchedEffect(item.poster) {
                        if (!CoilImageLoader.isValidImageUrl(item.poster)) {
                            android.util.Log.w(
                                "MediaScreen",
                                "无效的图片URL: ${item.title} - ${item.poster}"
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrokenImage,
                                contentDescription = "图片加载失败",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "图片不可用",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            )
            
            // 信息
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 评分
                    if (item.rate.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "评分",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = item.rate,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // 年份
                    if (item.year.isNotBlank()) {
                        Text(
                            text = item.year,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 加载指示器
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "加载中...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyState(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = "空状态",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "暂无内容",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onRetry
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "重试",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}