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
    ): ApiResultWithCookie<LoginResponseDto> {
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
        return ktorClient.post<LogoutResponseDto>(
            url = getApiUrl("/api/logout")
        ).result
    }
    
    /**
     * 获取服务器配置
     */
    suspend fun getServerConfig(): ApiResult<ServerConfigDto> {
        return ktorClient.get<ServerConfigDto>(
            url = getApiUrl("/api/server-config")
        ).result
    }

    // ============= 内容搜索API =============
    
    /**
     * 搜索影视资源
     */
    suspend fun searchMedia(query: String): ApiResult<SearchResponseDto> {
        return ktorClient.get<SearchResponseDto>(
            url = getApiUrl("/api/search"),
            params = mapOf("q" to query)
        ).result
    }
    
    /**
     * 获取影视详情
     */
    suspend fun getMediaDetail(id: String, source: String): ApiResult<MediaDetailDto> {
        return ktorClient.get<MediaDetailDto>(
            url = getApiUrl("/api/detail"),
            params = mapOf(
                "id" to id,
                "source" to source
            )
        ).result
    }

    // ============= 直播相关API =============
    
    /**
     * 获取直播频道列表
     */
    suspend fun getLiveChannels(source: String): ApiResult<LiveChannelsResponseDto> {
        return ktorClient.get<LiveChannelsResponseDto>(
            url = getApiUrl("/api/live/channels"),
            params = mapOf("source" to source)
        ).result
    }

    // ============= 示例API（保留用于参考） =============
    
    /**
     * 获取媒体列表示例
     */
    suspend fun getMediaList(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<MediaListResponse> {
        return ktorClient.get<MediaListResponse>(
            url = getApiUrl("/media/list"),
            params = mapOf(
                "page" to page.toString(),
                "pageSize" to pageSize.toString()
            )
        ).result
    }
    
    /**
     * 获取媒体详情示例
     */
    suspend fun getMediaDetailExample(id: String): ApiResult<MediaDetailResponse> {
        return ktorClient.get<MediaDetailResponse>(
            url = getApiUrl("/media/$id")
        ).result
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