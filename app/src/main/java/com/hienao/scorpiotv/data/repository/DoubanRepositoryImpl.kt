package com.hienao.scorpiotv.data.repository

import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.data.network.DoubanApiService
import com.hienao.scorpiotv.data.network.DoubanRecommendCategoriesResponse
import com.hienao.scorpiotv.data.network.DoubanResponseDto
import com.hienao.scorpiotv.data.network.NetworkDebugLogger

import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanRecommendCategories
import com.hienao.scorpiotv.domain.model.SortOption
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import com.hienao.scorpiotv.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * 豆瓣数据仓库实现类
 */
class DoubanRepositoryImpl(
    private val apiService: ApiService,
    private val doubanApiService: DoubanApiService,
    private val userRepository: UserRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher
) : DoubanRepository {

    override suspend fun getDoubanData(
        type: String,
        tag: String,
        pageSize: Int,
        pageStart: Int
    ): Flow<ApiResult<List<DoubanItem>>> = flow {
        emit(ApiResult.Loading)
        
        // 记录Repository层调用详情
        Log.d("DoubanRepository", "=== DoubanRepository调用详情 ===")
        Log.d("DoubanRepository", "方法: getDoubanData")
        Log.d("DoubanRepository", "参数:")
        Log.d("DoubanRepository", "  type: $type")
        Log.d("DoubanRepository", "  tag: $tag")
        Log.d("DoubanRepository", "  pageSize: $pageSize")
        Log.d("DoubanRepository", "  pageStart: $pageStart")
        
        // 获取服务器URL并设置到豆瓣API服务
        val serverUrl = userRepository.getServerUrl() ?: ""
        doubanApiService.setBaseUrl(serverUrl)
        Log.d("DoubanRepository", "服务器URL: $serverUrl")
        Log.d("DoubanRepository", "URL状态: ${if (serverUrl.isNotEmpty()) "已设置" else "未设置"}")
        
        // 获取认证Cookie
        val authCookie = userRepository.getAuthCookie()
        Log.d("DoubanRepository", "Cookie状态: ${if (authCookie != null) "已获取 (长度: ${authCookie.length})" else "未获取到Cookie"}")
        if (authCookie != null && authCookie.length > 20) {
            val maskedCookie = "${authCookie.take(10)}...${authCookie.takeLast(10)}"
            Log.d("DoubanRepository", "Cookie内容: $maskedCookie")
        }
        
        Log.d("DoubanRepository", "开始调用API...")
        val result = doubanApiService.getDoubanData(type, tag, pageSize, pageStart, authCookie)
        
        result.onSuccess { response ->
            Log.d("DoubanRepository", "✅ API调用成功，返回 ${response.list.size} 条数据")
            val doubanItems = response.list.map { it.mapToDomainModel() }
            emit(ApiResult.Success(doubanItems))
        }.onError { exception, message, code ->
            Log.e("DoubanRepository", "❌ API调用失败")
            Log.e("DoubanRepository", "错误码: $code")
            Log.e("DoubanRepository", "错误信息: $message")
            Log.e("DoubanRepository", "异常: ${exception?.message}")
            
            // 处理401错误或疑似401错误的情况
            val is401Error = code == 401 || 
                           (code == -1 && (message?.contains("401", ignoreCase = true) == true || 
                                          message?.contains("unauthorized", ignoreCase = true) == true ||
                                          message?.contains("认证", ignoreCase = true) == true))
            
            if (is401Error) {
                // 尝试从异常中获取更详细的信息
                val detailedStatusCode = when {
                    exception is io.ktor.client.plugins.ClientRequestException -> {
                        exception.response.status.value
                    }
                    code != null && code != -1 -> code
                    else -> 401 // 默认使用401
                }
                
                NetworkDebugLogger.log401ErrorDetails(
                    url = "${serverUrl.trimEnd('/')}/api/douban",
                    method = "GET",
                    params = mapOf(
                        "type" to type,
                        "tag" to tag,
                        "pageSize" to pageSize.toString(),
                        "pageStart" to pageStart.toString()
                    ),
                    authCookie = authCookie,
                    statusCode = detailedStatusCode
                )
            }
            
            emit(ApiResult.Error(exception, message, code))
        }
        
        Log.d("DoubanRepository", "==============================")
    }.flowOn(ioDispatcher)

    override suspend fun getDoubanCategories(
        kind: String,
        category: String,
        type: String,
        limit: Int,
        start: Int
    ): Flow<ApiResult<List<DoubanItem>>> = flow {
        emit(ApiResult.Loading)
        
        // 获取服务器URL并设置到豆瓣API服务
        val serverUrl = userRepository.getServerUrl() ?: ""
        doubanApiService.setBaseUrl(serverUrl)
        
        // 获取认证Cookie
        val authCookie = userRepository.getAuthCookie()
        val result = doubanApiService.getDoubanCategories(kind, category, type, limit, start, authCookie)
        
        result.onSuccess { response ->
            val doubanItems = response.list.map { it.mapToDomainModel() }
            emit(ApiResult.Success(doubanItems))
        }.onError { exception, message, code ->
            emit(ApiResult.Error(exception, message, code))
        }
    }.flowOn(ioDispatcher)

    override suspend fun getDoubanRecommends(
        kind: String,
        limit: Int,
        start: Int,
        category: String?,
        format: String?,
        region: String?,
        year: String?,
        platform: String?,
        sort: String?,
        label: String?
    ): Flow<ApiResult<List<DoubanItem>>> = flow {
        emit(ApiResult.Loading)
        
        // 获取服务器URL并设置到豆瓣API服务
        val serverUrl = userRepository.getServerUrl() ?: ""
        doubanApiService.setBaseUrl(serverUrl)
        
        // 获取认证Cookie
        val authCookie = userRepository.getAuthCookie()
        val result = doubanApiService.getDoubanRecommends(
            kind = kind,
            limit = limit,
            start = start,
            category = category,
            format = format,
            region = region,
            year = year,
            platform = platform,
            sort = sort,
            label = label,
            authCookie = authCookie
        )
        
        result.onSuccess { response ->
            val doubanItems = response.list.map { it.mapToDomainModel() }
            emit(ApiResult.Success(doubanItems))
        }.onError { exception, message, code ->
            emit(ApiResult.Error(exception, message, code))
        }
    }.flowOn(ioDispatcher)

    override suspend fun getDoubanRecommendCategories(
        kind: String
    ): Flow<ApiResult<DoubanRecommendCategories>> = flow {
        emit(ApiResult.Loading)
        
        // 获取服务器URL并设置到豆瓣API服务
        val serverUrl = userRepository.getServerUrl() ?: ""
        doubanApiService.setBaseUrl(serverUrl)
        
        // 获取认证Cookie
        val authCookie = userRepository.getAuthCookie()
        val result = doubanApiService.getDoubanRecommendCategories(kind, authCookie)
        
        result.onSuccess { response ->
            val recommendCategories = DoubanRecommendCategories(
                types = response.recommend_categories.types,
                regions = response.recommend_categories.regions,
                sorts = response.sorts.map { 
                    SortOption(
                        value = it.value,
                        label = it.label
                    )
                }
            )
            emit(ApiResult.Success(recommendCategories))
        }.onError { exception, message, code ->
            emit(ApiResult.Error(exception, message, code))
        }
    }.flowOn(ioDispatcher)
}

/**
 * 扩展函数，将DTO转换为领域模型
 */
fun com.hienao.scorpiotv.data.network.DoubanItemDto.mapToDomainModel(): DoubanItem {
    return DoubanItem(
        id = this.id,
        title = this.title,
        poster = this.poster,
        rate = this.rate,
        year = this.year
    )
}