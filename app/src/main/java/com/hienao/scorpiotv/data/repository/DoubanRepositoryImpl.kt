package com.hienao.scorpiotv.data.repository

import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.data.network.DoubanItemDto
import com.hienao.scorpiotv.data.network.DoubanRecommendCategoriesResponse
import com.hienao.scorpiotv.data.network.DoubanResponseDto
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanRecommendCategories
import com.hienao.scorpiotv.domain.model.MovieFilterState
import com.hienao.scorpiotv.domain.model.SortOption
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

/**
 * 豆瓣数据仓库实现类
 */
class DoubanRepositoryImpl(
    private val apiService: ApiService,
    private val ioDispatcher: CoroutineContext
) : DoubanRepository {

    override suspend fun getDoubanData(
        type: String,
        tag: String,
        pageSize: Int,
        pageStart: Int
    ): Flow<ApiResult<List<DoubanItem>>> = flow {
        emit(ApiResult.Loading)
        
        try {
            val result = apiService.getDoubanData(
                type = type,
                tag = tag,
                pageSize = pageSize,
                pageStart = pageStart
            )
            
            result.onSuccess { response ->
                val doubanItems = response.list.map { dto ->
                    DoubanItem(
                        id = dto.id,
                        title = dto.title,
                        poster = dto.poster,
                        rate = dto.rate,
                        year = dto.year
                    )
                }
                emit(ApiResult.Success(doubanItems))
            }.onError { exception, message, code ->
                emit(ApiResult.Error(exception, message, code))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e, e.message))
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
        
        try {
            val result = apiService.getDoubanCategories(
                kind = kind,
                category = category,
                type = type,
                limit = limit,
                start = start
            )
            
            result.onSuccess { response ->
                val doubanItems = response.list.map { dto ->
                    DoubanItem(
                        id = dto.id,
                        title = dto.title,
                        poster = dto.poster,
                        rate = dto.rate,
                        year = dto.year
                    )
                }
                emit(ApiResult.Success(doubanItems))
            }.onError { exception, message, code ->
                emit(ApiResult.Error(exception, message, code))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e, e.message))
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
        
        try {
            val result = apiService.getDoubanRecommends(
                kind = kind,
                limit = limit,
                start = start,
                category = category,
                format = format,
                region = region,
                year = year,
                platform = platform,
                sort = sort,
                label = label
            )
            
            result.onSuccess { response ->
                val doubanItems = response.list.map { dto ->
                    DoubanItem(
                        id = dto.id,
                        title = dto.title,
                        poster = dto.poster,
                        rate = dto.rate,
                        year = dto.year
                    )
                }
                emit(ApiResult.Success(doubanItems))
            }.onError { exception, message, code ->
                emit(ApiResult.Error(exception, message, code))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e, e.message))
        }
    }.flowOn(ioDispatcher)

    override suspend fun getDoubanRecommendCategories(
        kind: String
    ): Flow<ApiResult<DoubanRecommendCategories>> = flow {
        emit(ApiResult.Loading)
        
        try {
            val result = apiService.getDoubanRecommendCategories(kind)
            
            result.onSuccess { response ->
                val recommendCategories = DoubanRecommendCategories(
                    types = response.recommend_categories.types,
                    regions = response.recommend_categories.regions,
                    sorts = response.sorts.map { sort ->
                        SortOption(
                            value = sort.value,
                            label = sort.label
                        )
                    }
                )
                emit(ApiResult.Success(recommendCategories))
            }.onError { exception, message, code ->
                emit(ApiResult.Error(exception, message, code))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e, e.message))
        }
    }.flowOn(ioDispatcher)
}