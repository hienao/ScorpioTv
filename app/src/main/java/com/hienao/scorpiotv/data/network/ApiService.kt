package com.hienao.scorpiotv.data.network

import kotlinx.serialization.Serializable

/**
 * API服务接口
 * 基于DecoTV API文档实现网络请求
 */
class ApiService(private val ktorClient: KtorClient) {
    
    // 动态baseUrl，通过构造函数或setter设置
    var baseUrl: String = ""
        private set
    
    /**
     * 设置基础URL
     */
    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }
    
    /**
     * 获取完整的API URL
     */
    private fun getApiUrl(endpoint: String): String {
        return "$baseUrl$endpoint"
    }
    
    // ============= 认证相关API =============
    
    /**
     * 用户登录
     */
    suspend fun login(
        username: String?,
        password: String
    ): ApiResult<LoginResponseDto> {
        val requestBody = if (username != null) {
            LoginRequestDto(username, password)
        } else {
            // LocalStorage模式只需要密码
            LoginRequestDto(null, password)
        }
        
        return ktorClient.post(
            url = getApiUrl("/api/login"),
            body = requestBody
        )
    }
    
    /**
     * 用户登出
     */
    suspend fun logout(): ApiResult<LogoutResponseDto> {
        return ktorClient.post(
            url = getApiUrl("/api/logout")
        )
    }
    
    /**
     * 获取服务器配置
     */
    suspend fun getServerConfig(): ApiResult<ServerConfigDto> {
        return ktorClient.get(
            url = getApiUrl("/api/server-config")
        )
    }

    // ============= 内容搜索API =============
    
    /**
     * 搜索影视资源
     */
    suspend fun searchMedia(query: String): ApiResult<SearchResponseDto> {
        return ktorClient.get(
            url = getApiUrl("/api/search"),
            params = mapOf("q" to query)
        )
    }
    
    /**
     * 获取影视详情
     */
    suspend fun getMediaDetail(id: String, source: String): ApiResult<MediaDetailDto> {
        return ktorClient.get(
            url = getApiUrl("/api/detail"),
            params = mapOf(
                "id" to id,
                "source" to source
            )
        )
    }

    // ============= 豆瓣数据API =============
    
    /**
     * 获取豆瓣影视数据
     */
    suspend fun getDoubanData(
        type: String,
        tag: String,
        pageSize: Int = 16,
        pageStart: Int = 0
    ): ApiResult<DoubanResponseDto> {
        return ktorClient.get(
            url = getApiUrl("/api/douban"),
            params = mapOf(
                "type" to type,
                "tag" to tag,
                "pageSize" to pageSize.toString(),
                "pageStart" to pageStart.toString()
            )
        )
    }
    
    /**
     * 获取豆瓣分类详情
     */
    suspend fun getDoubanCategories(
        kind: String,
        category: String,
        type: String,
        limit: Int = 20,
        start: Int = 0
    ): ApiResult<DoubanResponseDto> {
        return ktorClient.get(
            url = getApiUrl("/api/douban/categories"),
            params = mapOf(
                "kind" to kind,
                "category" to category,
                "type" to type,
                "limit" to limit.toString(),
                "start" to start.toString()
            )
        )
    }
    
    /**
     * 获取豆瓣智能推荐
     */
    suspend fun getDoubanRecommends(
        kind: String,
        limit: Int = 20,
        start: Int = 0,
        category: String? = null,
        format: String? = null,
        region: String? = null,
        year: String? = null,
        platform: String? = null,
        sort: String? = null,
        label: String? = null
    ): ApiResult<DoubanResponseDto> {
        val params = mutableMapOf<String, String>()
        params["kind"] = kind
        params["limit"] = limit.toString()
        params["start"] = start.toString()
        
        // 添加可选参数
        category?.let { params["category"] = it }
        format?.let { params["format"] = it }
        region?.let { params["region"] = it }
        year?.let { params["year"] = it }
        platform?.let { params["platform"] = it }
        sort?.let { params["sort"] = it }
        label?.let { params["label"] = it }
        
        return ktorClient.get(
            url = getApiUrl("/api/douban/recommends"),
            params = params
        )
    }
    
    /**
     * 获取豆瓣推荐分类数据
     */
    suspend fun getDoubanRecommendCategories(
        kind: String
    ): ApiResult<DoubanRecommendCategoriesResponse> {
        return ktorClient.get(
            url = getApiUrl("/api/douban/recommends"),
            params = mapOf(
                "kind" to kind,
                "limit" to "1", // 只需要获取分类数据，不需要内容
                "start" to "0"
            )
        )
    }

    // ============= 直播相关API =============
    
    /**
     * 获取直播频道列表
     */
    suspend fun getLiveChannels(source: String): ApiResult<LiveChannelsResponseDto> {
        return ktorClient.get(
            url = getApiUrl("/api/live/channels"),
            params = mapOf("source" to source)
        )
    }

    // ============= 示例API（保留用于参考） =============
    
    /**
     * 获取媒体列表示例
     */
    suspend fun getMediaList(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<MediaListResponse> {
        return ktorClient.get(
            url = getApiUrl("/media/list"),
            params = mapOf(
                "page" to page.toString(),
                "pageSize" to pageSize.toString()
            )
        )
    }
    
    /**
     * 获取媒体详情示例
     */
    suspend fun getMediaDetailExample(id: String): ApiResult<MediaDetailResponse> {
        return ktorClient.get(
            url = getApiUrl("/media/$id")
        )
    }
    
    /**
     * 更新用户信息示例
     */
    suspend fun updateUserInfo(
        userId: String,
        userInfo: UserInfoRequest
    ): ApiResult<UserInfoResponse> {
        return ktorClient.put(
            url = getApiUrl("/user/$userId"),
            body = userInfo
        )
    }
    
    /**
     * 删除媒体示例
     */
    suspend fun deleteMedia(id: String): ApiResult<DeleteResponse> {
        return ktorClient.delete(
            url = getApiUrl("/media/$id")
        )
    }
}

// ============= 请求/响应数据模型 =============

// ============= 认证相关 =============

@Serializable
data class LoginRequestDto(
    val username: String? = null,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val ok: Boolean,
    val error: String? = null
)

@Serializable
data class LogoutResponseDto(
    val ok: Boolean
)

@Serializable
data class ServerConfigDto(
    val SiteName: String = "",
    val StorageType: String = "",
    val Version: String = ""
)

// ============= 搜索相关 =============

@Serializable
data class SearchResponseDto(
    val results: List<SearchItemDto> = emptyList()
)

@Serializable
data class SearchItemDto(
    val vod_id: String,
    val vod_name: String,
    val vod_pic: String,
    val vod_remarks: String,
    val type_name: String,
    val source_key: String,
    val source_name: String
)

@Serializable
data class MediaDetailDto(
    val vod_id: String,
    val vod_name: String,
    val vod_pic: String,
    val vod_content: String,
    val vod_play_from: String,
    val vod_play_url: String,
    val type_name: String
)

// ============= 豆瓣相关 =============

@Serializable
data class DoubanResponseDto(
    val code: Int = 200,
    val message: String = "",
    val list: List<DoubanItemDto> = emptyList()
)

@Serializable
data class DoubanItemDto(
    val id: String,
    val title: String,
    val poster: String,
    val rate: String,
    val year: String
)

// ============= 直播相关 =============

@Serializable
data class LiveChannelsResponseDto(
    val success: Boolean = false,
    val data: List<LiveChannelDto> = emptyList()
)

@Serializable
data class LiveChannelDto(
    val name: String,
    val url: String,
    val logo: String,
    val group: String,
    val tvg_id: String
)

// ============= 示例数据模型（保留用于参考） =============

@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val username: String
)

@Serializable
data class MediaListResponse(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val items: List<MediaItemDto>
)

@Serializable
data class MediaItemDto(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val description: String?
)

@Serializable
data class MediaDetailResponse(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val description: String?,
    val videoUrl: String,
    val duration: Long,
    val createTime: Long
)

@Serializable
data class UserInfoRequest(
    val username: String?,
    val email: String?,
    val avatar: String?
)

@Serializable
data class UserInfoResponse(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String?
)

@Serializable
data class DeleteResponse(
    val success: Boolean,
    val message: String
)

// ============= 豆瓣推荐分类相关 =============

@Serializable
data class DoubanRecommendCategoriesResponse(
    val recommend_categories: RecommendCategoriesDto,
    val sorts: List<SortItemDto>
)

@Serializable
data class RecommendCategoriesDto(
    val types: List<String>,
    val regions: List<String>
)

@Serializable
data class SortItemDto(
    val value: String,
    val label: String
)