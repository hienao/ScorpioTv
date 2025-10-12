package com.hienao.scorpiotv.presentation.screen.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.hienao.scorpiotv.domain.model.DoubanItem
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

/**
 * 播放器页面Screen
 */
@Composable
fun PlayerScreen(
    mediaItem: DoubanItem? = null,
    videoUrl: String = "",
    onBack: () -> Unit = {},
    viewModel: PlayerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current

    // 设置媒体项
    LaunchedEffect(mediaItem) {
        if (mediaItem != null && videoUrl.isNotBlank()) {
            viewModel.handleIntent(PlayerContract.UiIntent.SetMediaItem(mediaItem, videoUrl))
        }
    }

    // 处理UI效果
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is PlayerContract.UiEffect.ShowToast -> {
                    // 这里可以显示Toast
                }
                is PlayerContract.UiEffect.ShowError -> {
                    // 错误信息已经在UI状态中处理
                }
                is PlayerContract.UiEffect.NavigateBack -> {
                    onBack()
                }
                is PlayerContract.UiEffect.ShareVideo -> {
                    // 实现分享功能
                }
            }
        }
    }

    // 播放器界面
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.errorMessage != null) {
            ErrorState(
                error = uiState.errorMessage ?: "未知错误",
                onRetry = {
                    viewModel.handleIntent(PlayerContract.UiIntent.ClearError)
                }
            )
        } else {
            PlayerContent(
                uiState = uiState,
                onIntent = viewModel::handleIntent,
                onBack = onBack,
                configuration = configuration
            )
        }
    }
}

/**
 * 播放器内容
 */
@Composable
private fun PlayerContent(
    uiState: PlayerContract.UiState,
    onIntent: (PlayerContract.UiIntent) -> Unit,
    onBack: () -> Unit,
    configuration: android.content.res.Configuration
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部控制栏
        PlayerTopBar(
            title = uiState.title,
            onBack = onBack,
            onShare = {
                // 这里应该触发分享功能
                // 由于ShareVideo不在UiEffect中，我们暂时注释掉
                // onIntent(PlayerContract.UiIntent.ShareVideo)
            },
            onMore = { /* 更多菜单 */ }
        )

        // 视频播放器区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 这里应该集成GSYVideoPlayer
            // 由于GSYVideoPlayer是基于传统View的，需要使用AndroidView包装
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    // 这里创建GSYVideoPlayer实例
                    // 由于GSYVideoPlayer的集成比较复杂，这里先提供一个占位符
                    android.widget.VideoView(context).apply {
                        setVideoPath(uiState.videoUrl)
                        setOnPreparedListener {
                            // 视频准备完成
                            onIntent(PlayerContract.UiIntent.Play)
                        }
                        setOnCompletionListener { mp ->
                            // 播放完成
                            onIntent(PlayerContract.UiIntent.Stop)
                        }
                        // 使用setOnErrorListener替代setOnError
                        setOnErrorListener { mediaPlayer, what, extra ->
                            // 播放错误
                            onIntent(PlayerContract.UiIntent.ClearError)
                            true
                        }
                    }
                }
            )

            // 播放控制覆盖层
            PlayerControls(
                uiState = uiState,
                onIntent = onIntent
            )
        }

        // 底部进度条
        PlayerBottomBar(
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            onSeek = { position ->
                onIntent(PlayerContract.UiIntent.SeekToPosition(position))
            },
            onPlayPause = {
                if (uiState.isPlaying) {
                    onIntent(PlayerContract.UiIntent.Pause)
                } else {
                    onIntent(PlayerContract.UiIntent.Play)
                }
            },
            onFullscreen = {
                onIntent(PlayerContract.UiIntent.ToggleFullscreen)
            }
        )
    }
}

/**
 * 顶部控制栏
 */
@Composable
private fun PlayerTopBar(
    title: String,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "返回"
            )
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        Row {
            IconButton(
                onClick = onShare
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "分享"
                )
            }
            
            IconButton(
                onClick = onMore
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "更多"
                )
            }
        }
    }
}

/**
 * 播放控制覆盖层
 */
@Composable
private fun PlayerControls(
    uiState: PlayerContract.UiState,
    onIntent: (PlayerContract.UiIntent) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 播放/暂停按钮（居中）
        IconButton(
            onClick = {
                if (uiState.isPlaying) {
                    onIntent(PlayerContract.UiIntent.Pause)
                } else {
                    onIntent(PlayerContract.UiIntent.Play)
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
        ) {
            Icon(
                imageVector = if (uiState.isPlaying) {
                    Icons.Filled.Pause
                } else {
                    Icons.Filled.PlayArrow
                },
                contentDescription = if (uiState.isPlaying) {
                    "暂停"
                } else {
                    "播放"
                },
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // 左侧控制按钮
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.Replay) }
            ) {
                Icon(
                    Icons.Filled.Replay,
                    contentDescription = "重播"
                )
            }

            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.SeekToPosition(0L)) }
            ) {
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "快进"
                )
            }
        }

        // 右侧控制按钮
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.ToggleFullscreen) }
            ) {
                Icon(
                    imageVector = if (uiState.isFullscreen) {
                        Icons.Filled.FullscreenExit
                    } else {
                        Icons.Filled.Fullscreen
                    },
                    contentDescription = "全屏"
                )
            }

            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.SetVolume(0.8f)) }
            ) {
                Icon(
                    Icons.Filled.VolumeDown,
                    contentDescription = "音量减"
                )
            }

            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.SetVolume(1.2f)) }
            ) {
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = "音量加"
                )
            }
        }

        // 亮度控制（右侧）
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = 16.dp, vertical = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.SetBrightness(0.3f)) }
            ) {
                Icon(
                    Icons.Filled.BrightnessLow,
                    contentDescription = "降低亮度"
                )
            }

            IconButton(
                onClick = { onIntent(PlayerContract.UiIntent.SetBrightness(0.7f)) }
            ) {
                Icon(
                    Icons.Filled.BrightnessHigh,
                    contentDescription = "提高亮度"
                )
            }
        }
    }
}

/**
 * 底部进度条
 */
@Composable
private fun PlayerBottomBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onFullscreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 进度条
        LinearProgressIndicator(
            progress = if (duration > 0) {
                (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
            } else {
                0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )

        // 时间显示和控制
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(currentPosition),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 进度拖拽区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                // 这里可以实现进度拖拽功能
            }

            Text(
                text = formatTime(duration),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 播放控制按钮
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            IconButton(
                onClick = { onSeek(0L) }
            ) {
                Icon(
                    Icons.Filled.FastRewind,
                    contentDescription = "快退"
                )
            }

            IconButton(
                onClick = onPlayPause
            ) {
                Icon(
                    imageVector = if (currentPosition > 0) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
                    contentDescription = "播放/暂停"
                )
            }

            IconButton(
                onClick = { onSeek(duration) }
            ) {
                Icon(
                    Icons.Filled.FastForward,
                    contentDescription = "快进"
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
                text = "正在加载播放器...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 错误状态
 */
@Composable
private fun ErrorState(
    error: String,
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
                imageVector = Icons.Filled.Error,
                contentDescription = "错误",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = error,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRetry
            ) {
                Text("重试")
            }
        }
    }
}

/**
 * 格式化时间
 */
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
        else -> String.format("00:%02d", seconds)
    }
}