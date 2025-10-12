package com.hienao.scorpiotv.presentation.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

/**
 * 主界面Screen
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 处理UI效果
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MainContract.UiEffect.ShowToast -> {
                    // 这里可以显示Toast，暂时用其他方式处理
                }
                is MainContract.UiEffect.ShowError -> {
                    // 错误信息已经在UI状态中处理
                }
                is MainContract.UiEffect.NavigateToLogin -> {
                    // 导航到登录页面
                }
                is MainContract.UiEffect.NavigateToSettings -> {
                    // 导航到设置页面
                }
                is MainContract.UiEffect.LoginRequired -> {
                    // 需要登录
                }
            }
        }
    }

    // 初始化
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // 主界面布局
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧导航栏
        NavigationSidebar(
            navigationItems = uiState.navigationItems,
            selectedNavigationId = uiState.selectedNavigationId,
            currentUser = uiState.currentUser,
            onNavigationSelected = { navigationId ->
                viewModel.handleIntent(MainContract.UiIntent.SelectNavigation(navigationId))
            },
            onLogout = {
                viewModel.handleIntent(MainContract.UiIntent.Logout)
            },
            modifier = Modifier.width(200.dp)
        )

        // 右侧内容区域
        ContentArea(
            selectedNavigationId = uiState.selectedNavigationId,
            currentUser = uiState.currentUser,
            modifier = Modifier.fillMaxSize()
        )
    }

    // 登录弹窗
    if (uiState.showLoginDialog) {
        com.hienao.scorpiotv.presentation.screen.login.LoginDialog(
            onDismiss = {
                viewModel.handleIntent(MainContract.UiIntent.HideLoginDialog)
            },
            onLoginSuccess = {
                viewModel.onLoginSuccess()
            }
        )
    }

    // 错误提示
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 这里可以显示Snackbar或其他错误提示
            viewModel.handleIntent(MainContract.UiIntent.ClearError)
        }
    }
}

/**
 * 左侧导航栏
 */
@Composable
private fun NavigationSidebar(
    navigationItems: List<com.hienao.scorpiotv.domain.model.NavigationItem>,
    selectedNavigationId: String,
    currentUser: com.hienao.scorpiotv.domain.model.User?,
    onNavigationSelected: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 应用标题
            Text(
                text = "ScorpioTV",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 导航项列表
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                navigationItems.forEach { item ->
                    NavigationItemRow(
                        item = item,
                        isSelected = item.id == selectedNavigationId,
                        onSelected = onNavigationSelected
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 用户信息区域
            currentUser?.let { user ->
                if (user.isLoggedIn) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = user.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = user.serverUrl,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "退出登录",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("退出登录")
                    }
                }
            }
        }
    }
}

/**
 * 导航项行
 */
@Composable
private fun NavigationItemRow(
    item: com.hienao.scorpiotv.domain.model.NavigationItem,
    isSelected: Boolean,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelected(item.id) },
                role = Role.Tab
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getIconForNavigation(item.icon),
            contentDescription = item.title,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * 右侧内容区域
 */
@Composable
private fun ContentArea(
    selectedNavigationId: String,
    currentUser: com.hienao.scorpiotv.domain.model.User?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when (selectedNavigationId) {
            "movie", "tv_series", "anime", "variety" -> {
                // 媒体内容页面
                val mediaType = when (selectedNavigationId) {
                    "tv_series" -> "tv"
                    "anime" -> "anime"
                    "variety" -> "show"
                    else -> selectedNavigationId
                }
                com.hienao.scorpiotv.presentation.screen.media.MediaScreen(
                    mediaType = mediaType
                )
            }
            "live" -> {
                // 直播页面
                com.hienao.scorpiotv.presentation.screen.live.LiveScreen()
            }
            "search" -> {
                // 搜索页面
                com.hienao.scorpiotv.presentation.screen.search.SearchScreen(
                    onNavigateToPlayer = { doubanItem ->
                        // 这里可以导航到播放器页面
                        // 暂时不实现，因为需要更复杂的导航逻辑
                    }
                )
            }
            "settings" -> {
                // 设置页面
                com.hienao.scorpiotv.presentation.screen.settings.SettingsScreen()
            }
            else -> {
                // 未知页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "未知页面",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 根据导航项图标获取对应的Icon
 */
private fun getIconForNavigation(iconName: String): ImageVector {
    return when (iconName) {
        "movie" -> Icons.Filled.Movie
        "tv" -> Icons.Filled.Tv
        "anime" -> Icons.Filled.Star
        "variety" -> Icons.Filled.EmojiEvents
        "live" -> Icons.Filled.LiveTv
        "search" -> Icons.Filled.Search
        "settings" -> Icons.Filled.Settings
        else -> Icons.Filled.Menu
    }
}

/**
 * 获取导航项标题
 */
private fun getNavigationTitle(navigationId: String): String {
    return when (navigationId) {
        "movie" -> "电影"
        "tv_series" -> "剧集"
        "anime" -> "动漫"
        "variety" -> "综艺"
        "live" -> "直播"
        "search" -> "搜索"
        "settings" -> "设置"
        else -> "未知页面"
    }
}

/**
 * 获取导航项描述
 */
private fun getNavigationDescription(navigationId: String, currentUser: com.hienao.scorpiotv.domain.model.User?): String {
    if (navigationId == "settings" && (currentUser == null || !currentUser.isLoggedIn)) {
        return "请先登录后访问设置页面"
    }
    
    return when (navigationId) {
        "movie" -> "这里是电影内容区域\n将展示热门电影和分类内容"
        "tv_series" -> "这里是剧集内容区域\n将展示热门电视剧和分类内容"
        "anime" -> "这里是动漫内容区域\n将展示热门动漫和分类内容"
        "variety" -> "这里是综艺内容区域\n将展示热门综艺和分类内容"
        "live" -> "这里是直播内容区域\n将展示直播频道列表"
        "search" -> "这里是搜索内容区域\n可以搜索影视资源"
        "settings" -> "这里是设置页面\n可以管理应用设置和用户信息"
        else -> "该功能正在开发中..."
    }
}