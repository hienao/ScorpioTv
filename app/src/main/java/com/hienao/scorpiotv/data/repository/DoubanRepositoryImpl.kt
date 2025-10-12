package com.hienao.scorpiotv.data.repository

import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.data.network.DoubanApiService
import com.hienao.scorpiotv.data.network.DoubanRecommendCategoriesResponse
import com.hienao.scorpiotv.data.network.DoubanResponseDto

import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanRecommendCategories
import com.hienao.scorpiotv.domain.model.SortOption
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import com.hienao.scorpiotv.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

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
        
        // 获取服务器URL并设置到豆瓣API服务
        val serverUrl = userRepository.getServerUrl() ?: ""
        doubanApiService.setBaseUrl(serverUrl)
        
        // 获取认证Cookie
        val authCookie = userRepository.getAuthCookie()
        val result = doubanApiService.getDoubanData(type, tag, pageSize, pageStart, authCookie)
        
        result.onSuccess { response ->
            val doubanItems = response.list.map { it.mapToDomainModel() }
            emit(ApiResult.Success(doubanItems))
        }.onError { exception, message, code ->
            emit(ApiResult.Error(exception, message, code))
        }
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