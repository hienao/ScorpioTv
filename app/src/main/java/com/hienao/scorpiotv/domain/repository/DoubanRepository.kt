package com.hienao.scorpiotv.domain.repository

import com.hienao.scorpiotv.data.network.ApiResult
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanRecommendCategories
import kotlinx.coroutines.flow.Flow

/**
 * 豆瓣数据仓库接口
 * 定义豆瓣数据访问的抽象接口
 */
interface DoubanRepository {
    
    /**
     * 获取豆瓣影视数据
     * @param type 影视类型 (movie, tv, anime, show)
     * @param tag 标签分类
     * @param pageSize 每页数量
     * @param pageStart 起始位置
     * @return 豆瓣响应结果
     */
    suspend fun getDoubanData(
        type: String,
        tag: String,
        pageSize: Int = 16,
        pageStart: Int = 0
    ): Flow<ApiResult<List<DoubanItem>>>
    
    /**
     * 获取豆瓣分类详情
     * @param kind 影视类型 (tv, movie, anime, show)
     * @param category 主分类
     * @param type 子分类类型
     * @param limit 每页数量
     * @param start 起始位置
     * @return 豆瓣响应结果
     */
    suspend fun getDoubanCategories(
        kind: String,
        category: String,
        type: String,
        limit: Int = 20,
        start: Int = 0
    ): Flow<ApiResult<List<DoubanItem>>>
    
    /**
     * 获取豆瓣智能推荐
     * @param kind 影视类型 (tv, movie, anime, show)
     * @param limit 每页数量
     * @param start 起始位置
     * @param category 主分类筛选
     * @param format 形式筛选
     * @param region 地区筛选
     * @param year 年份筛选
     * @param platform 平台筛选
     * @param sort 排序方式
     * @param label 标签筛选
     * @return 豆瓣响应结果
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
    ): Flow<ApiResult<List<DoubanItem>>>
    
    /**
     * 获取豆瓣推荐分类数据
     * @param kind 影视类型 (movie, tv, anime, show)
     * @return 推荐分类数据
     */
    suspend fun getDoubanRecommendCategories(kind: String): Flow<ApiResult<DoubanRecommendCategories>>
}