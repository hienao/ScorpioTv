package com.hienao.scorpiotv.presentation.screen.player

import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.presentation.base.BaseContract

/**
 * 播放器页面的MVI Contract
 * 定义了播放器页面的状态、意图和效果
 */
interface PlayerContract {
    
    /**
     * 播放器页面的UI状态
     */
    data class UiState(
        val mediaItem: DoubanItem? = null,
        val videoUrl: String = "",
        val title: String = "",
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val isBuffering: Boolean = false,
        val currentPosition: Long = 0L,
        val duration: Long = 0L,
        val errorMessage: String? = null,
        val isFullscreen: Boolean = false,
        val playbackSpeed: Float = 1.0f,
        val volume: Float = 1.0f,
        val brightness: Float = 0.5f
    ) : BaseContract.UiState
    
    /**
     * 播放器页面的用户意图
     */
    sealed class UiIntent : BaseContract.UiIntent {
        data class SetMediaItem(val item: DoubanItem, val videoUrl: String) : UiIntent()
        object Play : UiIntent()
        object Pause : UiIntent()
        object Stop : UiIntent()
        object SeekTo : UiIntent()
        data class SeekToPosition(val position: Long) : UiIntent()
        object ToggleFullscreen : UiIntent()
        data class SetPlaybackSpeed(val speed: Float) : UiIntent()
        data class SetVolume(val volume: Float) : UiIntent()
        data class SetBrightness(val brightness: Float) : UiIntent()
        object Replay : UiIntent()
        object Back : UiIntent()
        object ClearError : UiIntent()
    }
    
    /**
     * 播放器页面的UI效果（一次性事件）
     */
    sealed class UiEffect : BaseContract.UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class ShowError(val message: String) : UiEffect()
        object NavigateBack : UiEffect()
        data class ShareVideo(val title: String, val url: String) : UiEffect()
    }
}