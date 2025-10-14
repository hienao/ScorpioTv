package com.hienao.scorpiotv.data.network

import kotlinx.serialization.Serializable
import android.util.Log

/**
 * 豆瓣API服务接口
 * 使用携带Cookie的认证HttpClient访问豆瓣接口（根据API文档，豆瓣接口需要认证）
 */
class DoubanApiService(private val ktorClient: KtorClient) {
    
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
    
    // ============= 豆瓣数据API =============
    
    /**
     * 获取豆瓣影视数据
     */
    suspend fun getDoubanData(
        type: String,
        tag: String,
        pageSize: Int = 16,
        pageStart: Int = 0,
        authCookie: String? = null
    ): ApiResult<DoubanResponseDto> {
        // 记录API调用详情
        Log.d("DoubanApi", "=== 调用豆瓣影视数据API ===")
        Log.d("DoubanApi", "Base URL: $baseUrl")
        Log.d("DoubanApi", "完整URL: ${getApiUrl("/api/douban")}")
        Log.d("DoubanApi", "参数:")
        Log.d("DoubanApi", "  type: $type")
        Log.d("DoubanApi", "  tag: $tag")
        Log.d("DoubanApi", "  pageSize: $pageSize")
        Log.d("DoubanApi", "  pageStart: $pageStart")
        Log.d("DoubanApi", "认证状态: ${if (authCookie != null) "已设置Cookie (长度: ${authCookie.length})" else "未设置Cookie"}")
        Log.d("DoubanApi", "========================")
        
        return ktorClient.get<DoubanResponseDto>(
            url = getApiUrl("/api/douban"),
            params = mapOf(
                "type" to type,
                "tag" to tag,
                "pageSize" to pageSize.toString(),
                "pageStart" to pageStart.toString()
            ),
            authCookie = authCookie
        ).result
    }
    
    /**
     * 获取豆瓣分类详情
     */
    suspend fun getDoubanCategories(
        kind: String,
        category: String,
        type: String,
        limit: Int = 20,
        start: Int = 0,
        authCookie: String? = null
    ): ApiResult<DoubanResponseDto> {
        return ktorClient.get<DoubanResponseDto>(
            url = getApiUrl("/api/douban/categories"),
            params = mapOf(
                "kind" to kind,
                "category" to category,
                "type" to type,
                "limit" to limit.toString(),
                "start" to start.toString()
            ),
            authCookie = authCookie
        ).result
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
        label: String? = null,
        authCookie: String? = null
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
        
        return ktorClient.get<DoubanResponseDto>(
            url = getApiUrl("/api/douban/recommends"),
            params = params,
            authCookie = authCookie
        ).result
    }
    
    /**
     * 获取豆瓣推荐分类数据
     */
    suspend fun getDoubanRecommendCategories(
        kind: String,
        authCookie: String? = null
    ): ApiResult<DoubanRecommendCategoriesResponse> {
        return ktorClient.get<DoubanRecommendCategoriesResponse>(
            url = getApiUrl("/api/douban/recommends"),
            params = mapOf(
                "kind" to kind,
                "limit" to "1", // 只需要获取分类数据，不需要内容
                "start" to "0"
            ),
            authCookie = authCookie
        ).result
    }
}

// ============= 豆瓣相关数据模型 =============

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