package com.hienao.scorpiotv.data.repository

import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.domain.model.DoubanFilter
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanResponse
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 豆瓣数据仓库实现类
 */
class DoubanRepositoryImpl(
    private val apiService: ApiService,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : DoubanRepository {

    override suspend fun getDoubanData(
        type: String,
        tag: String,
        pageSize: Int,
        pageStart: Int
    ): Result<DoubanResponse> = withContext(dispatcher) {
        try {
            val result = apiService.getDoubanData(type, tag, pageSize, pageStart)
            
            when (result) {
                is ApiResult.Success -> {
                    val response = DoubanResponse(
                        code = result.data.code,
                        message = result.data.message,
                        items = result.data.list.map { dto ->
                            DoubanItem(
                                id = dto.id,
                                title = dto.title,
                                poster = dto.poster,
                                rate = dto.rate,
                                year = dto.year,
                                type = type
                            )
                        },
                        total = result.data.list.size,
                        hasMore = result.data.list.size == pageSize
                    )
                    Result.success(response)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.message ?: "获取豆瓣数据失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("加载中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDoubanCategories(
        kind: String,
        category: String,
        type: String,
        limit: Int,
        start: Int
    ): Result<DoubanResponse> = withContext(dispatcher) {
        try {
            val result = apiService.getDoubanCategories(kind, category, type, limit, start)
            
            when (result) {
                is ApiResult.Success -> {
                    val response = DoubanResponse(
                        code = result.data.code,
                        message = result.data.message,
                        items = result.data.list.map { dto ->
                            DoubanItem(
                                id = dto.id,
                                title = dto.title,
                                poster = dto.poster,
                                rate = dto.rate,
                                year = dto.year,
                                type = kind,
                                subtype = type
                            )
                        },
                        total = result.data.list.size,
                        hasMore = result.data.list.size == limit
                    )
                    Result.success(response)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.message ?: "获取豆瓣分类数据失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("加载中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
    ): Result<DoubanResponse> = withContext(dispatcher) {
        try {
            val result = apiService.getDoubanCategories(
                kind = kind,
                category = category ?: "热门",
                type = format ?: "热门",
                limit = limit,
                start = start
            )
            
            when (result) {
                is ApiResult.Success -> {
                    val response = DoubanResponse(
                        code = result.data.code,
                        message = result.data.message,
                        items = result.data.list.map { dto ->
                            DoubanItem(
                                id = dto.id,
                                title = dto.title,
                                poster = dto.poster,
                                rate = dto.rate,
                                year = dto.year,
                                type = kind,
                                subtype = format ?: ""
                            )
                        },
                        total = result.data.list.size,
                        hasMore = result.data.list.size == limit
                    )
                    Result.success(response)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.message ?: "获取豆瓣推荐数据失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("加载中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDataByFilter(filter: DoubanFilter): Result<DoubanResponse> {
        return if (filter.tag == "top250") {
            getTop250Data(filter.type, filter.limit, filter.start)
        } else {
            getDoubanData(filter.type, filter.tag, filter.limit, filter.start)
        }
    }

    override suspend fun searchMedia(query: String): Result<List<DoubanItem>> = withContext(dispatcher) {
        try {
            val result = apiService.searchMedia(query)
            
            when (result) {
                is ApiResult.Success -> {
                    val items = result.data.results.map { dto ->
                        DoubanItem(
                            id = dto.vod_id,
                            title = dto.vod_name,
                            poster = dto.vod_pic,
                            rate = dto.vod_remarks,
                            year = "",
                            type = dto.type_name,
                            subtype = dto.source_name
                        )
                    }
                    Result.success(items)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.message ?: "搜索失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("搜索中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMediaDetail(id: String, source: String): Result<DoubanItem> = withContext(dispatcher) {
        try {
            val result = apiService.getMediaDetail(id, source)
            
            when (result) {
                is ApiResult.Success -> {
                    val item = DoubanItem(
                        id = result.data.vod_id,
                        title = result.data.vod_name,
                        poster = result.data.vod_pic,
                        rate = "", // MediaDetailDto中没有vod_remarks字段
                        year = "",
                        type = result.data.type_name,
                        subtype = source
                    )
                    Result.success(item)
                }
                is ApiResult.Error -> {
                    Result.failure(Exception(result.message ?: "获取详情失败"))
                }
                is ApiResult.Loading -> {
                    Result.failure(Exception("加载中..."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHotData(type: String, limit: Int): Result<List<DoubanItem>> {
        val result = getDoubanData(type, "热门", limit, 0)
        return result.map { it.items }
    }

    override suspend fun getTop250Data(type: String, limit: Int, start: Int): Result<DoubanResponse> {
        return getDoubanData(type, "top250", limit, start)
    }
}