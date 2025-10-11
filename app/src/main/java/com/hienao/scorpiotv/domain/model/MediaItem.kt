package com.hienao.scorpiotv.domain.model

/**
 * 媒体项领域模型
 * 表示应用中的媒体内容
 */
data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: Long,
    val category: String,
    val tags: List<String>,
    val isFavorite: Boolean = false,
    val viewCount: Long = 0,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 媒体分类领域模型
 */
data class MediaCategory(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val itemCount: Int
)

/**
 * 用户偏好设置领域模型
 */
data class UserPreferences(
    val userId: String,
    val favoriteCategories: List<String>,
    val playbackQuality: VideoQuality,
    val autoPlay: Boolean,
    val notificationsEnabled: Boolean,
    val themeMode: ThemeMode
)

/**
 * 视频质量枚举
 */
enum class VideoQuality {
    AUTO,
    LOW,
    MEDIUM,
    HIGH,
    ULTRA
}

/**
 * 主题模式枚举
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}