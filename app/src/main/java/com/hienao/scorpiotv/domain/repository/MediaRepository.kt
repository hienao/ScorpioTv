package com.hienao.scorpiotv.domain.repository

import com.hienao.scorpiotv.domain.model.MediaItem
import com.hienao.scorpiotv.domain.model.MediaCategory
import kotlinx.coroutines.flow.Flow

/**
 * 媒体仓库接口
 * 定义媒体数据访问的抽象接口
 */
interface MediaRepository {
    
    /**
     * 获取媒体项列表
     * @param category 分类ID，可选
     * @param limit 限制数量，可选
     * @return 媒体项列表的Flow
     */
    suspend fun getMediaItems(category: String? = null, limit: Int? = null): Result<List<MediaItem>>
    
    /**
     * 获取媒体项详情
     * @param id 媒体项ID
     * @return 媒体项详情的Result
     */
    suspend fun getMediaItemById(id: String): Result<MediaItem>
    
    /**
     * 搜索媒体项
     * @param query 搜索关键词
     * @param limit 限制数量，可选
     * @return 搜索结果列表的Result
     */
    suspend fun searchMediaItems(query: String, limit: Int? = null): Result<List<MediaItem>>
    
    /**
     * 获取媒体分类列表
     * @return 媒体分类列表的Result
     */
    suspend fun getMediaCategories(): Result<List<MediaCategory>>
    
    /**
     * 获取推荐媒体项
     * @param userId 用户ID，可选
     * @param limit 限制数量，可选
     * @return 推荐媒体项列表的Result
     */
    suspend fun getRecommendedMedia(userId: String? = null, limit: Int? = null): Result<List<MediaItem>>
    
    /**
     * 获取热门媒体项
     * @param limit 限制数量，可选
     * @return 热门媒体项列表的Result
     */
    suspend fun getTrendingMedia(limit: Int? = null): Result<List<MediaItem>>
    
    /**
     * 切换媒体项的收藏状态
     * @param mediaId 媒体项ID
     * @param isFavorite 是否收藏
     * @return 操作结果的Result
     */
    suspend fun toggleFavorite(mediaId: String, isFavorite: Boolean): Result<Unit>
    
    /**
     * 获取用户收藏的媒体项
     * @param userId 用户ID
     * @param limit 限制数量，可选
     * @return 收藏媒体项列表的Result
     */
    suspend fun getFavoriteMedia(userId: String, limit: Int? = null): Result<List<MediaItem>>
    
    /**
     * 记录媒体项播放历史
     * @param userId 用户ID
     * @param mediaId 媒体项ID
     * @param progress 播放进度
     * @return 操作结果的Result
     */
    suspend fun recordPlaybackHistory(userId: String, mediaId: String, progress: Float): Result<Unit>
    
    /**
     * 获取播放历史
     * @param userId 用户ID
     * @param limit 限制数量，可选
     * @return 播放历史列表的Result
     */
    suspend fun getPlaybackHistory(userId: String, limit: Int? = null): Result<List<MediaItem>>
    
    /**
     * 获取媒体项的观看进度
     * @param userId 用户ID
     * @param mediaId 媒体项ID
     * @return 观看进度的Flow
     */
    fun getPlaybackProgress(userId: String, mediaId: String): Flow<Float>
}