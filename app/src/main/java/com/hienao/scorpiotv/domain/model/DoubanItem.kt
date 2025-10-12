package com.hienao.scorpiotv.domain.model

/**
 * 豆瓣影视项领域模型
 */
data class DoubanItem(
    val id: String,
    val title: String,
    val poster: String,
    val rate: String,
    val year: String,
    val type: String = "", // 电影类型标签
    val subtype: String = "" // 子类型
)

/**
 * 豆瓣分类项领域模型
 */
data class DoubanCategory(
    val name: String,
    val type: String, // movie, tv, anime, show
    val tags: List<String>
)

/**
 * 豆瓣筛选条件领域模型
 */
data class DoubanFilter(
    val type: String = "movie", // movie, tv, anime, show
    val category: String = "热门", // 主分类
    val tag: String = "热门", // 标签
    val limit: Int = 20,
    val start: Int = 0
)

/**
 * 豆瓣响应领域模型
 */
data class DoubanResponse(
    val code: Int = 200,
    val message: String = "",
    val items: List<DoubanItem> = emptyList(),
    val total: Int = 0,
    val hasMore: Boolean = false
)

/**
 * 预定义的豆瓣分类
 */
object DoubanCategories {
    
    // 电影分类
    val MOVIE_CATEGORIES = listOf(
        "热门", "最新", "经典", "豆瓣高分", "喜剧", "动作", "爱情", "科幻", 
        "悬疑", "恐怖", "动画", "top250"
    )
    
    // 电视剧分类
    val TV_CATEGORIES = listOf(
        "热门", "美剧", "英剧", "韩剧", "日剧", "国产剧", "港剧", "台剧"
    )
    
    // 动漫分类
    val ANIME_CATEGORIES = listOf(
        "热门", "日本", "国产", "欧美", "番剧", "动画电影"
    )
    
    // 综艺分类
    val VARIETY_CATEGORIES = listOf(
        "热门", "国产", "韩国", "日本", "欧美", "音乐", "真人秀"
    )
    
    /**
     * 根据类型获取分类列表
     */
    fun getCategoriesByType(type: String): List<String> {
        return when (type) {
            "movie" -> MOVIE_CATEGORIES
            "tv" -> TV_CATEGORIES
            "anime" -> ANIME_CATEGORIES
            "show" -> VARIETY_CATEGORIES
            else -> emptyList()
        }
    }
    
    /**
     * 获取默认分类
     */
    fun getDefaultCategory(type: String): String {
        return when (type) {
            "movie", "tv", "anime", "show" -> "热门"
            else -> "热门"
        }
    }
}

/**
 * 豆瓣类型映射
 */
object DoubanTypeMapping {
    
    /**
     * 获取类型的中文名称
     */
    fun getTypeDisplayName(type: String): String {
        return when (type) {
            "movie" -> "电影"
            "tv" -> "剧集"
            "anime" -> "动漫"
            "show" -> "综艺"
            else -> "未知"
        }
    }
    
    /**
     * 获取所有支持的类型
     */
    fun getAllTypes(): List<Pair<String, String>> {
        return listOf(
            "movie" to "电影",
            "tv" to "剧集", 
            "anime" to "动漫",
            "show" to "综艺"
        )
    }
}