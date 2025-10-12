package com.hienao.scorpiotv.presentation.screen.player

import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import com.hienao.scorpiotv.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * 播放器页面的ViewModel
 * 处理播放器页面的业务逻辑
 */
class PlayerViewModel(
    private val doubanRepository: DoubanRepository
) : BaseViewModel<PlayerContract.UiState, PlayerContract.UiIntent, PlayerContract.UiEffect>(
    initialState = PlayerContract.UiState()
) {

    override fun handleIntent(intent: PlayerContract.UiIntent) {
        when (intent) {
            is PlayerContract.UiIntent.SetMediaItem -> setMediaItem(intent.item, intent.videoUrl)
            is PlayerContract.UiIntent.Play -> play()
            is PlayerContract.UiIntent.Pause -> pause()
            is PlayerContract.UiIntent.Stop -> stop()
            is PlayerContract.UiIntent.SeekTo -> seekTo()
            is PlayerContract.UiIntent.SeekToPosition -> seekToPosition(intent.position)
            is PlayerContract.UiIntent.ToggleFullscreen -> toggleFullscreen()
            is PlayerContract.UiIntent.SetPlaybackSpeed -> setPlaybackSpeed(intent.speed)
            is PlayerContract.UiIntent.SetVolume -> setVolume(intent.volume)
            is PlayerContract.UiIntent.SetBrightness -> setBrightness(intent.brightness)
            is PlayerContract.UiIntent.Replay -> replay()
            is PlayerContract.UiIntent.Back -> back()
            is PlayerContract.UiIntent.ClearError -> clearError()
        }
    }

    /**
     * 设置媒体项和视频URL
     */
    private fun setMediaItem(item: DoubanItem, videoUrl: String) {
        updateState {
            copy(
                mediaItem = item,
                videoUrl = videoUrl,
                title = item.title,
                isLoading = false,
                errorMessage = null,
                isPlaying = false,
                currentPosition = 0L,
                duration = 0L
            )
        }
        
        // 这里可以初始化播放器
        initializePlayer()
    }

    /**
     * 播放
     */
    private fun play() {
        updateState {
            copy(
                isPlaying = true,
                isLoading = false,
                errorMessage = null
            )
        }
        
        // 这里控制GSYVideoPlayer播放
        sendEffect(PlayerContract.UiEffect.ShowToast("开始播放"))
    }

    /**
     * 暂停
     */
    private fun pause() {
        updateState {
            copy(isPlaying = false)
        }
        
        // 这里控制GSYVideoPlayer暂停
        sendEffect(PlayerContract.UiEffect.ShowToast("已暂停"))
    }

    /**
     * 停止
     */
    private fun stop() {
        updateState {
            copy(
                isPlaying = false,
                currentPosition = 0L,
                isLoading = false
            )
        }
        
        // 这里控制GSYVideoPlayer停止
        sendEffect(PlayerContract.UiEffect.NavigateBack)
    }

    /**
     * 跳转到当前位置
     */
    private fun seekTo() {
        // 这里实现跳转逻辑
        sendEffect(PlayerContract.UiEffect.ShowToast("跳转到指定位置"))
    }

    /**
     * 跳转到指定位置
     */
    private fun seekToPosition(position: Long) {
        updateState {
            copy(currentPosition = position)
        }
        
        // 这里控制GSYVideoPlayer跳转
    }

    /**
     * 切换全屏
     */
    private fun toggleFullscreen() {
        updateState {
            copy(isFullscreen = !isFullscreen)
        }
        
        val message = if (currentState.isFullscreen) "退出全屏" else "进入全屏"
        sendEffect(PlayerContract.UiEffect.ShowToast(message))
    }

    /**
     * 设置播放速度
     */
    private fun setPlaybackSpeed(speed: Float) {
        updateState {
            copy(playbackSpeed = speed)
        }
        
        sendEffect(PlayerContract.UiEffect.ShowToast("播放速度: ${speed}x"))
    }

    /**
     * 设置音量
     */
    private fun setVolume(volume: Float) {
        updateState {
            copy(volume = volume)
        }
        
        sendEffect(PlayerContract.UiEffect.ShowToast("音量: ${(volume * 100).toInt()}%"))
    }

    /**
     * 设置亮度
     */
    private fun setBrightness(brightness: Float) {
        updateState {
            copy(brightness = brightness)
        }
        
        sendEffect(PlayerContract.UiEffect.ShowToast("亮度: ${(brightness * 100).toInt()}%"))
    }

    /**
     * 重播
     */
    private fun replay() {
        updateState {
            copy(
                currentPosition = 0L,
                isPlaying = true,
                errorMessage = null
            )
        }
        
        sendEffect(PlayerContract.UiEffect.ShowToast("重新播放"))
    }

    /**
     * 返回
     */
    private fun back() {
        sendEffect(PlayerContract.UiEffect.NavigateBack)
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
     * 初始化播放器
     */
    private fun initializePlayer() {
        updateState {
            copy(isLoading = true)
        }
        
        launchViewModelScope {
            try {
                // 这里可以初始化GSYVideoPlayer
                // 模拟初始化完成
                updateState {
                    copy(
                        isLoading = false,
                        duration = 100000L // 模拟100秒时长
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = e.message ?: "播放器初始化失败"
                    )
                }
                sendEffect(PlayerContract.UiEffect.ShowError("播放器初始化失败: ${e.message}"))
            }
        }
    }

    /**
     * 更新播放进度
     */
    fun updatePlaybackProgress(position: Long, duration: Long) {
        updateState {
            copy(
                currentPosition = position,
                duration = duration,
                isBuffering = false
            )
        }
    }

    /**
     * 更新缓冲状态
     */
    fun updateBufferingState(isBuffering: Boolean) {
        updateState {
            copy(isBuffering = isBuffering)
        }
    }

    /**
     * 播放完成
     */
    fun onPlaybackComplete() {
        updateState {
            copy(
                isPlaying = false,
                currentPosition = duration
            )
        }
        
        sendEffect(PlayerContract.UiEffect.ShowToast("播放完成"))
    }

    /**
     * 播放错误
     */
    fun onPlaybackError(error: String) {
        updateState {
            copy(
                isPlaying = false,
                errorMessage = error
            )
        }
        
        sendEffect(PlayerContract.UiEffect.ShowError("播放错误: $error"))
    }

    /**
     * 分享视频
     */
    fun shareVideo() {
        val currentState = currentState
        if (currentState.videoUrl.isNotBlank()) {
            sendEffect(PlayerContract.UiEffect.ShareVideo(
                title = currentState.title,
                url = currentState.videoUrl
            ))
        }
    }
}