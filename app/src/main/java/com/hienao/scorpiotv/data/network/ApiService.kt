package com.hienao.scorpiotv.data.network

import kotlinx.serialization.Serializable

/**
 * API服务接口示例
 * 展示如何使用KtorClient进行网络请求
 */
class ApiService(private val ktorClient: KtorClient) {
    
    companion object {
        // API基础URL - 可以通过配置文件或BuildConfig配置
        private const val BASE_URL = "https://api.example.com"
    }
    
    /**
     * 获取媒体列表示例
     */
    suspend fun getMediaList(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<MediaListResponse> {
        return ktorClient.get(
            url = "$BASE_URL/media/list",
            params = mapOf(
                "page" to page.toString(),
                "pageSize" to pageSize.toString()
            )
        )
    }
    
    /**
     * 获取媒体详情示例
     */
    suspend fun getMediaDetail(id: String): ApiResult<MediaDetailResponse> {
        return ktorClient.get(
            url = "$BASE_URL/media/$id"
        )
    }
    
    /**
     * 登录示例
     */
    suspend fun login(
        username: String,
        password: String
    ): ApiResult<LoginResponse> {
        return ktorClient.post(
            url = "$BASE_URL/auth/login",
            body = LoginRequest(username, password)
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
            url = "$BASE_URL/user/$userId",
            body = userInfo
        )
    }
    
    /**
     * 删除媒体示例
     */
    suspend fun deleteMedia(id: String): ApiResult<DeleteResponse> {
        return ktorClient.delete(
            url = "$BASE_URL/media/$id"
        )
    }
}

// ============= 请求/响应数据模型示例 =============

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

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