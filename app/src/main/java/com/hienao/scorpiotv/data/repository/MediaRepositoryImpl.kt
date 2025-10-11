package com.hienao.scorpiotv.data.repository

import com.hienao.scorpiotv.domain.model.MediaCategory
import com.hienao.scorpiotv.domain.model.MediaItem
import com.hienao.scorpiotv.domain.repository.MediaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/**
 * MediaRepository的实现类
 * 提供媒体数据的具体实现（目前使用模拟数据）
 */
class MediaRepositoryImpl(
    private val dispatcher: CoroutineDispatcher
) : MediaRepository {
    
    // 模拟数据
    private val sampleMediaItems = listOf(
        MediaItem(
            id = "1",
            title = "精彩电影1",
            description = "这是一部非常精彩的电影，讲述了一个动人的故事。",
            thumbnailUrl = "https://example.com/thumb1.jpg",
            videoUrl = "https://example.com/video1.mp4",
            duration = 7200000, // 2小时
            category = "电影",
            tags = listOf("动作", "冒险", "科幻"),
            viewCount = 1000000,
            rating = 4.5f
        ),
        MediaItem(
            id = "2",
            title = "热门剧集2",
            description = "一部备受好评的电视剧，剧情跌宕起伏。",
            thumbnailUrl = "https://example.com/thumb2.jpg",
            videoUrl = "https://example.com/video2.mp4",
            duration = 2700000, // 45分钟
            category = "电视剧",
            tags = listOf("剧情", "悬疑"),
            viewCount = 500000,
            rating = 4.2f
        ),
        MediaItem(
            id = "3",
            title = "纪录片3",
            description = "探索自然奥秘的精彩纪录片。",
            thumbnailUrl = "https://example.com/thumb3.jpg",
            videoUrl = "https://example.com/video3.mp4",
            duration = 3600000, // 1小时
            category = "纪录片",
            tags = listOf("自然", "科学"),
            viewCount = 200000,
            rating = 4.8f
        )
    )
    
    private val sampleCategories = listOf(
        MediaCategory(
            id = "1",
            name = "电影",
            description = "各种类型的电影",
            iconUrl = "https://example.com/movie_icon.png",
            itemCount = 1
        ),
        MediaCategory(
            id = "2",
            name = "电视剧",
            description = "热门电视剧集",
            iconUrl = "https://example.com/tv_icon.png",
            itemCount = 1
        ),
        MediaCategory(
            id = "3",
            name = "纪录片",
            description = "教育性纪录片",
            iconUrl = "https://example.com/doc_icon.png",
            itemCount = 1
        )
    )
    
    override suspend fun getMediaItems(category: String?, limit: Int?): Result<List<MediaItem>> {
        return withContext(dispatcher) {
            try {
                // 模拟网络延迟
                delay(1000)
                
                var items = sampleMediaItems
                
                // 按分类过滤
                if (!category.isNullOrBlank()) {
                    items = items.filter { it.category == category }
                }
                
                // 限制数量
                if (limit != null && limit > 0) {
                    items = items.take(limit)
                }
                
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getMediaItemById(id: String): Result<MediaItem> {
        return withContext(dispatcher) {
            try {
                delay(500)
                val item = sampleMediaItems.find { it.id == id }
                if (item != null) {
                    Result.success(item)
                } else {
                    Result.failure(NoSuchElementException("Media item not found: $id"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun searchMediaItems(query: String, limit: Int?): Result<List<MediaItem>> {
        return withContext(dispatcher) {
            try {
                delay(800)
                var items = sampleMediaItems.filter { 
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
                
                if (limit != null && limit > 0) {
                    items = items.take(limit)
                }
                
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getMediaCategories(): Result<List<MediaCategory>> {
        return withContext(dispatcher) {
            try {
                delay(500)
                Result.success(sampleCategories)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getRecommendedMedia(userId: String?, limit: Int?): Result<List<MediaItem>> {
        return withContext(dispatcher) {
            try {
                delay(1200)
                var items = sampleMediaItems.sortedByDescending { it.rating }
                
                if (limit != null && limit > 0) {
                    items = items.take(limit)
                }
                
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getTrendingMedia(limit: Int?): Result<List<MediaItem>> {
        return withContext(dispatcher) {
            try {
                delay(1000)
                var items = sampleMediaItems.sortedByDescending { it.viewCount }
                
                if (limit != null && limit > 0) {
                    items = items.take(limit)
                }
                
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun toggleFavorite(mediaId: String, isFavorite: Boolean): Result<Unit> {
        return withContext(dispatcher) {
            try {
                delay(300)
                // 这里应该更新本地数据库或发送网络请求
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getFavoriteMedia(userId: String, limit: Int?): Result<List<MediaItem>> {
        return withContext(dispatcher) {
            try {
                delay(800)
                // 模拟返回收藏的媒体项
                var items = sampleMediaItems.filter { it.isFavorite }
                
                if (limit != null && limit > 0) {
                    items = items.take(limit)
                }
                
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun recordPlaybackHistory(userId: String, mediaId: String, progress: Float): Result<Unit> {
        return withContext(dispatcher) {
            try {
                delay(200)
                // 这里应该记录播放历史到本地数据库
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getPlaybackHistory(userId: String, limit: Int?): Result<List<MediaItem>> {
        return withContext(dispatcher) {
            try {
                delay(600)
                // 模拟返回播放历史
                var items = sampleMediaItems.take(2) // 模拟最近播放的2个
                
                if (limit != null && limit > 0) {
                    items = items.take(limit)
                }
                
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override fun getPlaybackProgress(userId: String, mediaId: String): Flow<Float> {
        // 模拟返回播放进度
        return flowOf(0.3f) // 30%的播放进度
    }
}