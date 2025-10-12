package com.hienao.scorpiotv.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hienao.scorpiotv.domain.model.User
import com.hienao.scorpiotv.presentation.base.CollectAsEffect
import org.koin.androidx.compose.koinViewModel

/**
 * 设置页面Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 处理UI效果
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingsContract.UiEffect.ShowToast -> {
                    // 这里可以显示Toast
                }
                is SettingsContract.UiEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                is SettingsContract.UiEffect.NavigateToAbout -> {
                    onNavigateToAbout()
                }
                is SettingsContract.UiEffect.NavigateToPrivacyPolicy -> {
                    onNavigateToPrivacyPolicy()
                }
                is SettingsContract.UiEffect.NavigateToTermsOfService -> {
                    onNavigateToTermsOfService()
                }
            }
        }
    }

    // 初始化
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // 设置页面内容
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    text = "设置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { /* 返回 */ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.handleIntent(SettingsContract.UiIntent.Refresh)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "刷新"
                    )
                }
            }
        )

        // 内容区域
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息部分
                item {
                    UserInfoSection(
                        user = uiState.currentUser,
                        onEditProfile = { /* 编辑个人资料 */ }
                    )
                }

                // 通用设置部分
                item {
                    SectionHeader(title = "通用设置")
                }

                item {
                    SettingsItem(
                        title = "深色模式",
                        subtitle = "开启深色模式主题",
                        icon = Icons.Filled.DarkMode,
                        trailing = {
                            Switch(
                                checked = uiState.darkMode,
                                onCheckedChange = {
                                    viewModel.handleIntent(SettingsContract.UiIntent.ToggleDarkMode)
                                }
                            )
                        },
                        onClick = {
                            viewModel.handleIntent(SettingsContract.UiIntent.ToggleDarkMode)
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "通知",
                        subtitle = "接收推送通知",
                        icon = Icons.Filled.Notifications,
                        trailing = {
                            Switch(
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = {
                                    viewModel.handleIntent(SettingsContract.UiIntent.ToggleNotifications)
                                }
                            )
                        },
                        onClick = {
                            viewModel.handleIntent(SettingsContract.UiIntent.ToggleNotifications)
                        }
                    )
                }

                // 播放设置部分
                item {
                    SectionHeader(title = "播放设置")
                }

                item {
                    SettingsItem(
                        title = "自动播放",
                        subtitle = "自动播放下一个视频",
                        icon = Icons.Filled.PlayArrow,
                        trailing = {
                            Switch(
                                checked = uiState.autoPlayEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.handleIntent(SettingsContract.UiIntent.UpdateSetting("autoPlayEnabled", enabled))
                                }
                            )
                        },
                        onClick = {
                            val newValue = !uiState.autoPlayEnabled
                            viewModel.handleIntent(SettingsContract.UiIntent.UpdateSetting("autoPlayEnabled", newValue))
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "高清播放",
                        subtitle = "优先使用高清画质",
                        icon = Icons.Filled.HighQuality,
                        trailing = {
                            Switch(
                                checked = uiState.hdPlaybackEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.handleIntent(SettingsContract.UiIntent.UpdateSetting("hdPlaybackEnabled", enabled))
                                }
                            )
                        },
                        onClick = {
                            val newValue = !uiState.hdPlaybackEnabled
                            viewModel.handleIntent(SettingsContract.UiIntent.UpdateSetting("hdPlaybackEnabled", newValue))
                        }
                    )
                }

                // 存储设置部分
                item {
                    SectionHeader(title = "存储设置")
                }

                item {
                    SettingsItem(
                        title = "缓存大小",
                        subtitle = formatFileSize(uiState.cacheSize),
                        icon = Icons.Filled.Storage,
                        trailing = {
                            TextButton(
                                onClick = {
                                    viewModel.handleIntent(SettingsContract.UiIntent.ClearCache)
                                }
                            ) {
                                Text("清除")
                            }
                        },
                        onClick = {
                            viewModel.handleIntent(SettingsContract.UiIntent.ClearCache)
                        }
                    )
                }

                // 其他部分
                item {
                    SectionHeader(title = "其他")
                }

                item {
                    SettingsItem(
                        title = "关于",
                        subtitle = "版本 ${uiState.version} (${uiState.buildNumber})",
                        icon = Icons.Filled.Info,
                        onClick = {
                            viewModel.handleIntent(SettingsContract.UiIntent.ShowAbout)
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "隐私政策",
                        subtitle = "查看隐私政策",
                        icon = Icons.Filled.PrivacyTip,
                        onClick = {
                            viewModel.handleIntent(SettingsContract.UiIntent.ShowPrivacyPolicy)
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "服务条款",
                        subtitle = "查看服务条款",
                        icon = Icons.Filled.Description,
                        onClick = {
                            viewModel.handleIntent(SettingsContract.UiIntent.ShowTermsOfService)
                        }
                    )
                }

                // 登出按钮
                item {
                    if (uiState.currentUser != null) {
                        Button(
                            onClick = {
                                viewModel.handleIntent(SettingsContract.UiIntent.Logout)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "退出登录",
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }

                // 错误提示
                if (uiState.error != null) {
                    item {
                        ErrorMessage(
                            error = uiState.error ?: "未知错误",
                            onDismiss = {
                                viewModel.handleIntent(SettingsContract.UiIntent.ClearError)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 用户信息部分
 */
@Composable
private fun UserInfoSection(
    user: User?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            if (user != null) {
                // 这里应该显示用户头像
                // 由于我们还没有实现图片加载，这里使用一个占位符
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "用户头像",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 用户信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (user != null) {
                    Text(
                        text = user.username,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (user.isLoggedIn) "已登录" else "未登录",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (user.role != com.hienao.scorpiotv.domain.model.UserRole.USER) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "VIP",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = user.role.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                } else {
                    Text(
                        text = "未登录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "点击登录",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 编辑按钮
            IconButton(
                onClick = onEditProfile
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "编辑个人资料"
                )
            }
        }
    }
}

/**
 * 节标题
 */
@Composable
private fun SectionHeader(
    title: String
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 设置项
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 尾部内容
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 错误消息
 */
@Composable
private fun ErrorMessage(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = error,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
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
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "正在加载...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.1f GB".format(gb)
}