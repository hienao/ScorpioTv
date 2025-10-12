package com.hienao.scorpiotv.domain.model

/**
 * 直播频道领域模型
 */
data class LiveChannel(
    val id: String,
    val name: String,
    val logo: String,
    val desc: String,
    val url: String,
    val type: String, // live, movie, tv, anime, show
    val rate: String,
    val status: String, // online, offline
    val viewers: String,
    val language: String,
    val country: String,
    val category: String,
    val tags: List<String> = emptyList()
)

/**
 * 直播频道分类
 */
object LiveCategory {
    const val HOT = "热门"
    val ENTERTAINMENT = "娱乐"
    val SPORTS = "体育"
    val GAME = "游戏"
    val NEWS = "新闻"
    val EDUCATION = "教育"
    val MUSIC = "音乐"
    val LIFE = "生活"
    val TECH = "科技"
}

/**
 * 直播频道筛选条件
 */
data class LiveFilter(
    val type: String = "",
    val category: String = LiveCategory.HOT,
    val country: String = "",
    val language: String = "",
    val limit: Int = 20,
    val start: Int = 0
)

/**
 * 预定义的直播频道分类
 */
object LiveCategories {
    
    val TYPE_CATEGORIES = listOf(
        LiveCategory.HOT,
        LiveCategory.ENTERTAINMENT,
        LiveCategory.SPORTS,
        LiveCategory.GAME,
        LiveCategory.NEWS,
        LiveCategory.EDUCATION,
        LiveCategory.MUSIC,
        LiveCategory.LIFE,
        LiveCategory.TECH
    )
    
    /**
     * 根据类型获取分类列表
     */
    fun getCategoriesByType(type: String): List<String> {
        return when (type) {
            "live" -> TYPE_CATEGORIES
            "movie" -> listOf("热门", "最新", "经典", "豆瓣高分", "动作", "喜剧", "爱情", "科幻", "悬疑", "恐怖", "动画", "top250")
            "tv" -> listOf("热门", "美剧", "英剧", "韩剧", "日剧", "国产剧", "港剧", "台剧")
            "anime" -> listOf("热门", "日本", "国产", "欧美", "番剧", "动画电影")
            "show" -> listOf("热门", "国产", "韩国", "日本", "欧美", "音乐", "真人秀")
            else -> emptyList()
        }
    }
    
    /**
     * 获取默认分类
     */
    fun getDefaultCategory(type: String): String {
        return when (type) {
            "live", "movie", "tv", "anime", "show" -> LiveCategory.HOT
            else -> LiveCategory.HOT
        }
    }
}

/**
 * 直播频道类型映射
 */
object LiveTypeMapping {
    
    /**
     * 获取类型的中文名称
     */
    fun getTypeDisplayName(type: String): String {
        return when (type) {
            "live" -> "直播"
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
            "live" to "直播",
            "movie" to "电影",
            "tv" to "剧集",
            "anime" to "动漫",
            "show" to "综艺"
        )
    }
}