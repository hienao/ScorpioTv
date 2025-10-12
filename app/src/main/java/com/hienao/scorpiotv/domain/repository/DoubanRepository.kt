package com.hienao.scorpiotv.domain.repository

import com.hienao.scorpiotv.domain.model.DoubanFilter
import com.hienao.scorpiotv.domain.model.DoubanItem
import com.hienao.scorpiotv.domain.model.DoubanResponse

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
    ): Result<DoubanResponse>
    
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
    ): Result<DoubanResponse>
    
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
    ): Result<DoubanResponse>
    
    /**
     * 根据筛选条件获取数据
     * @param filter 筛选条件
     * @return 豆瓣响应结果
     */
    suspend fun getDataByFilter(filter: DoubanFilter): Result<DoubanResponse>
    
    /**
     * 搜索影视资源
     * @param query 搜索关键词
     * @return 搜索结果
     */
    suspend fun searchMedia(query: String): Result<List<DoubanItem>>
    
    /**
     * 获取影视详情
     * @param id 影视ID
     * @param source 采集源标识
     * @return 影视详情
     */
    suspend fun getMediaDetail(id: String, source: String): Result<DoubanItem>
    
    /**
     * 获取热门数据
     * @param type 影视类型
     * @param limit 数量限制
     * @return 热门数据列表
     */
    suspend fun getHotData(type: String, limit: Int = 10): Result<List<DoubanItem>>
    
    /**
     * 获取Top250数据
     * @param type 影视类型
     * @param limit 数量限制
     * @param start 起始位置
     * @return Top250数据列表
     */
    suspend fun getTop250Data(type: String, limit: Int = 50, start: Int = 0): Result<DoubanResponse>
}