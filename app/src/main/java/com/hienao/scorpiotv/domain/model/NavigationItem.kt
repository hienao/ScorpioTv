package com.hienao.scorpiotv.domain.model

/**
 * 导航项领域模型
 */
data class NavigationItem(
    val id: String,
    val title: String,
    val icon: String, // 图标资源名称
    val route: String,
    val isSelected: Boolean = false
)

/**
 * 预定义的导航项
 */
object NavigationItems {
    val MOVIE = NavigationItem(
        id = "movie",
        title = "电影",
        icon = "movie",
        route = "movie"
    )
    
    val TV_SERIES = NavigationItem(
        id = "tv_series",
        title = "剧集",
        icon = "tv",
        route = "tv_series"
    )
    
    val ANIME = NavigationItem(
        id = "anime",
        title = "动漫",
        icon = "anime",
        route = "anime"
    )
    
    val VARIETY = NavigationItem(
        id = "variety",
        title = "综艺",
        icon = "variety",
        route = "variety"
    )
    
    val LIVE = NavigationItem(
        id = "live",
        title = "直播",
        icon = "live",
        route = "live"
    )
    
    val SEARCH = NavigationItem(
        id = "search",
        title = "搜索",
        icon = "search",
        route = "search"
    )
    
    val SETTINGS = NavigationItem(
        id = "settings",
        title = "设置",
        icon = "settings",
        route = "settings"
    )
    
    /**
     * 获取所有导航项
     */
    fun getAllItems(): List<NavigationItem> {
        return listOf(
            MOVIE,
            TV_SERIES,
            ANIME,
            VARIETY,
            LIVE,
            SEARCH,
            SETTINGS
        )
    }
    
    /**
     * 根据ID获取导航项
     */
    fun getItemById(id: String): NavigationItem? {
        return getAllItems().find { it.id == id }
    }
}