package com.hienao.scorpiotv.domain.model

import kotlinx.serialization.Serializable

/**
 * 豆瓣智能推荐分类响应模型
 */
data class DoubanRecommendCategories(
    val types: List<String>,       // 类型选项，如：电影、电视剧
    val regions: List<String>,     // 地区选项，如：美国、韩国、日本
    val sorts: List<SortOption>    // 排序选项
)

/**
 * 排序选项
 */
data class SortOption(
    val value: String,             // 排序值
    val label: String              // 显示名称
)

/**
 * 电影页面筛选状态
 */
data class MovieFilterState(
    // 第一行分类筛选
    val primaryCategory: String = "全部",  // 全部/热门电影/最新电影/豆瓣高分/冷门佳片
    val primaryCategories: List<String> = listOf(
        "全部", "热门电影", "最新电影", "豆瓣高分", "冷门佳片"
    ),
    
    // 第二行条件筛选
    val filterType: String = "类型",  // 类型/地区/排序 (当第一行为全部时)
    val filterTypes: List<String> = listOf("类型", "地区", "排序"),
    
    // 类型选项
    val selectedType: String = "全部",
    val typeOptions: List<String> = emptyList(),
    
    // 地区选项
    val selectedRegion: String = "全部",
    val regionOptions: List<String> = listOf("全部", "华语", "欧美", "韩国", "日本"),
    
    // 排序选项
    val selectedSort: String = "推荐",
    val sortOptions: List<String> = emptyList(),
    
    // 是否使用推荐接口
    val useRecommendApi: Boolean = true
) {
    /**
     * 获取当前筛选条件对应的API参数
     */
    fun getApiParams(): Map<String, String> {
        return if (primaryCategory == "全部") {
            // 使用推荐接口
            when (filterType) {
                "类型" -> mapOf(
                    "kind" to "movie",
                    "format" to if (selectedType == "全部") "" else selectedType
                )
                "地区" -> mapOf(
                    "kind" to "movie",
                    "region" to if (selectedRegion == "全部") "" else selectedRegion
                )
                "排序" -> mapOf(
                    "kind" to "movie",
                    "sort" to if (selectedSort == "推荐") "" else selectedSort
                )
                else -> mapOf("kind" to "movie")
            }
        } else {
            // 使用普通豆瓣接口
            mapOf(
                "type" to "movie",
                "tag" to getCategoryTag()
            )
        }
    }
    
    /**
     * 获取分类标签
     */
    private fun getCategoryTag(): String {
        return when (primaryCategory) {
            "热门电影" -> "热门"
            "最新电影" -> "最新"
            "豆瓣高分" -> "豆瓣高分"
            "冷门佳片" -> "冷门佳片"
            else -> "热门"
        }
    }
    
    /**
     * 是否显示地区筛选（当第一行不是全部时）
     */
    fun shouldShowRegionFilter(): Boolean {
        return primaryCategory != "全部"
    }
}