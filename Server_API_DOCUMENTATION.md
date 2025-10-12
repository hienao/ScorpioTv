# DecoTV API 接口文档

## 概述

DecoTV 是一个基于 Next.js 的影视资源聚合网站，提供影视搜索、直播频道、TVBox配置生成等功能。本文档详细描述了所有后端API接口。

## 认证机制

### 认证方式
- **Cookie认证**: 使用 `auth` cookie 进行用户认证
- **签名验证**: 使用 HMAC-SHA256 对用户名进行签名验证
- **角色权限**: 支持三种角色 - `owner`(站长)、`admin`(管理员)、`user`(普通用户)

### Cookie格式说明
需要认证的接口必须在请求头中包含 `auth` cookie，格式如下：
```
Cookie: auth={"username":"用户名","password":"密码","signature":"签名","timestamp":时间戳}
```

**Cookie字段说明**:
- `username`: 用户名（数据库模式必需）
- `password`: 密码（必需）
- `signature`: HMAC-SHA256签名，用于验证请求合法性
- `timestamp`: 时间戳，用于防止重放攻击

**公开接口列表**（无需认证）:
- 直播接口：`/api/live/*`
- TVBox配置接口：`/api/tvbox/config`
- TVBox诊断接口：`/api/tvbox/diagnose`
- 代理接口：`/api/proxy/*`
- 系统配置接口：`/api/server-config`
- 版本检查接口：`/api/version/check`

**需要认证的接口**:
- 豆瓣数据接口：`/api/douban/*`
- 搜索接口：`/api/search/*`
- 用户数据接口：`/api/favorites/*`, `/api/playrecords/*`, `/api/searchhistory/*`
- 管理员接口：`/api/admin/*`
- 认证接口：`/api/login`, `/api/logout`, `/api/change-password`

### 存储模式
- **LocalStorage模式**: 仅使用固定密码验证，适合个人使用
- **数据库模式**: 支持多用户，使用用户名+密码验证

---

## 1. 认证相关接口

### 1.1 用户登录

**接口**: `POST /api/login`

**描述**: 用户登录认证

**请求参数**:
```json
{
  "username": "string",  // 用户名（数据库模式必需）
  "password": "string"   // 密码（必需）
}
```

**响应**:
```json
{
  "ok": true
}
```

**错误响应**:
```json
{
  "error": "密码错误",
  "ok": false
}
```

**特殊说明**:
- LocalStorage模式下只需要 `password`
- 数据库模式下需要 `username` 和 `password`
- 成功后会设置认证cookie，有效期7天

---

### 1.2 用户登出

**接口**: `POST /api/logout`

**描述**: 清除用户认证状态

**请求参数**: 无

**响应**:
```json
{
  "ok": true
}
```

---

### 1.3 修改密码

**接口**: `POST /api/change-password`

**描述**: 修改用户密码（仅数据库模式）

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
```json
{
  "newPassword": "string"  // 新密码
}
```

**响应**:
```json
{
  "ok": true
}
```

**错误响应**:
```json
{
  "error": "不支持本地存储模式修改密码"
}
```

---

## 2. 内容搜索接口

### 2.1 影视资源搜索

**接口**: `GET /api/search`

**描述**: 搜索影视资源

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
- `q` (query string): 搜索关键词

**响应**:
```json
{
  "results": [
    {
      "vod_id": "string",
      "vod_name": "string", 
      "vod_pic": "string",
      "vod_remarks": "string",
      "type_name": "string",
      "source_key": "string",
      "source_name": "string"
    }
  ]
}
```

**特殊说明**:
- 并发搜索多个采集源，20秒超时
- 自动过滤黄色内容（可配置）
- 支持缓存控制

---

### 2.2 获取影视详情

**接口**: `GET /api/detail`

**描述**: 获取影视资源详细信息

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
- `id` (query string): 影视ID
- `source` (query string): 采集源标识

**响应**:
```json
{
  "vod_id": "string",
  "vod_name": "string",
  "vod_pic": "string", 
  "vod_content": "string",
  "vod_play_from": "string",
  "vod_play_url": "string",
  "type_name": "string"
}
```

---

### 2.3 搜索建议

**接口**: `GET /api/search/suggestions`

**描述**: 获取搜索关键词建议

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
- `q` (query string): 搜索关键词前缀

**响应**:
```json
{
  "suggestions": [
    {
      "text": "string",
      "type": "exact|related|suggestion",
      "score": 1.5
    }
  ]
}
```

---

### 2.4 搜索历史管理

**接口**: `GET/POST/DELETE /api/searchhistory`

**描述**: 管理用户搜索历史

**认证**: 需要登录（通过Cookie中的auth字段）

#### GET - 获取搜索历史
**响应**:
```json
["关键词1", "关键词2", "关键词3"]
```

#### POST - 添加搜索历史
**请求参数**:
```json
{
  "keyword": "string"
}
```

#### DELETE - 删除搜索历史
**请求参数**:
- `keyword` (query string, 可选): 删除指定关键词，不传则清空全部

---

## 3. 用户数据接口

### 3.1 收藏管理

**接口**: `GET/POST/DELETE /api/favorites`

**描述**: 管理用户收藏

**认证**: 需要登录（通过Cookie中的auth字段）

#### GET - 获取收藏
**请求参数**:
- `key` (query string, 可选): 获取指定收藏，格式为 `source+id`

**响应**:
```json
{
  "source+id": {
    "title": "string",
    "poster": "string", 
    "source_name": "string",
    "save_time": 1234567890
  }
}
```

#### POST - 添加收藏
**请求参数**:
```json
{
  "key": "source+id",
  "favorite": {
    "title": "string",
    "poster": "string",
    "source_name": "string",
    "save_time": 1234567890
  }
}
```

#### DELETE - 删除收藏
**请求参数**:
- `key` (query string, 可选): 删除指定收藏，不传则清空全部

---

### 3.2 播放记录管理

**接口**: `GET/POST/DELETE /api/playrecords`

**描述**: 管理用户播放记录

**认证**: 需要登录（通过Cookie中的auth字段）

#### GET - 获取播放记录
**响应**:
```json
{
  "source+id": {
    "title": "string",
    "poster": "string",
    "source_name": "string", 
    "index": 1,
    "progress": 300,
    "save_time": 1234567890
  }
}
```

#### POST - 保存播放记录
**请求参数**:
```json
{
  "key": "source+id",
  "record": {
    "title": "string",
    "poster": "string",
    "source_name": "string",
    "index": 1,
    "progress": 300,
    "save_time": 1234567890
  }
}
```

#### DELETE - 删除播放记录
**请求参数**:
- `key` (query string, 可选): 删除指定记录，不传则清空全部

---

## 4. 豆瓣数据接口

### 4.1 豆瓣影视数据

**接口**: `GET /api/douban`

**描述**: 获取豆瓣影视数据，支持标签分类和Top250榜单

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
- `type` (query string, 必需): 影视类型，可选值：`tv`(电视剧)、`movie`(电影)、`anime`(动漫)、`show`(综艺)
- `tag` (query string, 必需): 标签分类，常用值：
  - 电视剧：`热门`、`美剧`、`英剧`、`韩剧`、`日剧`、`国产剧`
  - 电影：`热门`、`最新`、`经典`、`豆瓣高分`、`喜剧`、`动作`、`爱情`、`科幻`、`悬疑`、`恐怖`、`动画`
  - 动漫：`热门`、`日本`、`国产`、`欧美`、`番剧`、`动画电影`
  - 综艺：`热门`、`国产`、`韩国`、`日本`、`欧美`、`音乐`、`真人秀`
  - 特殊标签：`top250`(豆瓣Top250榜单)
- `pageSize` (query string, 可选): 每页数量，默认16，取值范围1-100
- `pageStart` (query string, 可选): 起始位置，默认0，用于分页

**参数验证规则**:
- `type` 必须是 `tv` 或 `movie`
- `pageSize` 必须在 1-100 之间
- `pageStart` 不能小于 0
- `tag` 为 `top250` 时会调用特殊的Top250接口

**响应**:
```json
{
  "code": 200,
  "message": "获取成功", 
  "list": [
    {
      "id": "string",
      "title": "string",
      "poster": "string",
      "rate": "string",
      "year": "string"
    }
  ]
}
```

---

### 4.2 豆瓣分类详情

**接口**: `GET /api/douban/categories`

**描述**: 获取豆瓣影视分类的详细数据，支持多维度筛选

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
- `kind` (query string, 必需): 影视类型，可选值：`tv`(电视剧)、`movie`(电影)、`anime`(动漫)、`show`(综艺)
- `category` (query string, 必需): 主分类，如 `热门`、`最新`、`经典` 等
- `type` (query string, 必需): 子分类类型，根据 `kind` 不同而有所区别
- `limit` (query string, 可选): 每页数量，默认20，取值范围1-100
- `start` (query string, 可选): 起始位置，默认0，用于分页

**参数验证规则**:
- `kind` 必须是 `tv`、`movie`、`anime` 或 `show`
- `category` 和 `type` 参数不能为空
- `limit` 必须在 1-100 之间
- `start` 不能小于 0

**常用参数组合示例**:
- 电视剧热门：`kind=tv&category=热门&type=热门`
- 电影最新：`kind=movie&category=最新&type=最新`
- 电视剧美剧：`kind=tv&category=美剧&type=美剧`

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "list": [
    {
      "id": "string",
      "title": "string",
      "poster": "string",
      "rate": "string",
      "year": "string"
    }
  ]
}
```

**特殊说明**:
- 此接口返回的是具体影视数据，而非分类列表
- 支持从 `card_subtitle` 字段提取年份信息
- 图片优先使用 `normal` 尺寸，回退到 `large` 尺寸

---

### 4.3 豆瓣智能推荐

**接口**: `GET /api/douban/recommends`

**描述**: 获取豆瓣个性化推荐内容，支持多维度筛选和排序

**认证**: 需要登录（通过Cookie中的auth字段）

**请求参数**:
- `kind` (query string, 必需): 影视类型，可选值：`tv`(电视剧)、`movie`(电影)、`anime`(动漫)、`show`(综艺)
- `limit` (query string, 可选): 每页数量，默认20，取值范围1-100
- `start` (query string, 可选): 起始位置，默认0，用于分页
- `category` (query string, 可选): 主分类筛选，`all` 表示不限制
- `format` (query string, 可选): 形式筛选，如 `电影`、`电视剧` 等，`all` 表示不限制
- `region` (query string, 可选): 地区筛选，如 `美国`、`韩国`、`日本` 等，`all` 表示不限制
- `year` (query string, 可选): 年份筛选，如 `2023`、`2022` 等，`all` 表示不限制
- `platform` (query string, 可选): 平台筛选，如 `Netflix`、`HBO` 等，`all` 表示不限制
- `sort` (query string, 可选): 排序方式，`T` 表示默认排序，其他值为自定义排序
- `label` (query string, 可选): 标签筛选，如 `高分`、`热门` 等，`all` 表示不限制

**参数处理规则**:
- 所有 `all` 值在内部会被转换为空字符串
- `sort` 参数值为 `T` 时会被转换为空字符串（使用默认排序）
- 筛选条件会组合成 `selected_categories` 对象传递给豆瓣API
- 多个筛选条件会通过 `tags` 参数组合传递

**常用参数组合示例**:
- 最新电影推荐：`kind=movie&category=最新&limit=20`
- 美剧推荐：`kind=tv&region=美国&format=美剧`
- 2023年高分电影：`kind=movie&year=2023&label=高分&sort=score`
- Netflix内容：`kind=movie&platform=Netflix&limit=30`
- 日本动漫推荐：`kind=anime&region=日本&category=热门&limit=20`
- 国产综艺推荐：`kind=show&region=中国&category=热门&format=综艺&limit=15`
- 番剧推荐：`kind=anime&format=电视剧&category=番剧&limit=25`

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "list": [
    {
      "id": "string",
      "title": "string",
      "poster": "string",
      "rate": "string",
      "year": "string"
    }
  ]
}
```

**特殊说明**:
- 接口会自动过滤只保留 `movie` 和 `tv` 类型的内容
- 支持复杂的筛选条件组合，可实现精准推荐
- 内部使用豆瓣推荐算法，结果具有个性化特征
- 评分格式化为一位小数显示

---

## 豆瓣接口使用场景和参数配合关系

### 4.4 接口选择指南

#### 场景1：获取基础分类内容
**推荐接口**: `GET /api/douban`
**适用情况**:
- 首页展示热门内容
- 简单的分类浏览
- 获取Top250榜单
- 快速加载和缓存

**参数配合示例**:
```bash
# 热门电影
/api/douban?type=movie&tag=热门&pageSize=20

# 豆瓣Top250电影
/api/douban?type=movie&tag=top250&pageSize=50&pageStart=0

# 热门美剧
/api/douban?type=tv&tag=美剧&pageSize=16

# 热门动漫
/api/douban?type=anime&tag=热门&pageSize=16

# 日本动漫
/api/douban?type=anime&tag=日本&pageSize=20

# 热门综艺
/api/douban?type=show&tag=热门&pageSize=16

# 韩国综艺
/api/douban?type=show&tag=韩国&pageSize=20
```

#### 场景2：详细分类浏览
**推荐接口**: `GET /api/douban/categories`
**适用情况**:
- 分类页面详细展示
- 需要更精确的分类筛选
- 获取包含年份信息的完整数据
- 分页浏览大量内容

**参数配合示例**:
```bash
# 最新电影分页
/api/douban/categories?kind=movie&category=最新&type=最新&limit=20&start=0

# 国产剧详情
/api/douban/categories?kind=tv&category=国产剧&type=国产剧&limit=30&start=40

# 经典电影
/api/douban/categories?kind=movie&category=经典&type=经典&limit=25&start=0
```

#### 场景3：个性化推荐
**推荐接口**: `GET /api/douban/recommends`
**适用情况**:
- 个性化推荐页面
- 复杂筛选条件组合
- 地区、平台、年份等多维度筛选
- 获取高质量推荐内容

**参数配合示例**:
```bash
# 2023年高分电影推荐
/api/douban/recommends?kind=movie&year=2023&label=高分&sort=score&limit=20

# Netflix美剧推荐
/api/douban/recommends?kind=tv&platform=Netflix&region=美国&limit=15

# 最新韩剧推荐
/api/douban/recommends?kind=tv&region=韩国&category=最新&format=韩剧&limit=25

# 综合筛选推荐
/api/douban/recommends?kind=movie&region=美国&year=2023&label=高分&platform=HBO&limit=30
```

### 4.5 参数配合最佳实践

#### 分页策略
- **首页/推荐页**: 使用较小的 `pageSize` (16-20)，快速加载
- **分类页**: 使用中等 `pageSize` (20-30)，平衡加载速度和内容量
- **搜索结果**: 使用较大 `pageSize` (50-100)，减少翻页次数

#### 缓存优化
- **热门内容**: 缓存时间较长，适合首页展示
- **最新内容**: 缓存时间较短，保证实时性
- **Top250**: 长期缓存，内容相对稳定

#### 错误处理
- 参数验证失败会返回400状态码和具体错误信息
- 豆瓣API超时时间为10秒
- 网络错误会返回500状态码和错误详情

#### 性能优化建议
1. **合理使用分页**: 避免一次加载过多数据
2. **选择合适的接口**: 根据场景选择最适合的接口
3. **参数验证**: 前端预先验证参数格式
4. **缓存策略**: 根据内容特性设置不同的缓存时间

### 4.6 实际应用示例

#### 首页内容加载流程
```javascript
// 1. 加载热门电影
GET /api/douban?type=movie&tag=热门&pageSize=16

// 2. 加载热门电视剧  
GET /api/douban?type=tv&tag=热门&pageSize=16

// 3. 加载热门动漫
GET /api/douban?type=anime&tag=热门&pageSize=16

// 4. 加载热门综艺
GET /api/douban?type=show&tag=热门&pageSize=16

// 5. 加载个性化推荐
GET /api/douban/recommends?kind=movie&limit=10&start=0
```

#### 分类页面加载流程
```javascript
// 1. 加载分类列表
GET /api/douban/categories?kind=movie&category=动作&type=动作&limit=20&start=0

// 2. 加载更多内容
GET /api/douban/categories?kind=movie&category=动作&type=动作&limit=20&start=20

// 3. 加载相关推荐
GET /api/douban/recommends?kind=movie&category=动作&limit=10
```

#### 搜索结果页推荐
```javascript
// 搜索无结果时的推荐
GET /api/douban/recommends?kind=movie&label=高分&limit=20

// 搜索有结果时的相关推荐
GET /api/douban/recommends?kind=movie&category=相关类型&limit=15
```

---

## 5. 直播相关接口

### 5.1 直播频道列表

**接口**: `GET /api/live/channels`

**描述**: 获取直播频道列表

**认证**: 无需认证（公开接口）

**请求参数**:
- `source` (query string): 直播源标识

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "name": "CCTV1",
      "url": "string",
      "logo": "string", 
      "group": "央视",
      "tvg-id": "cctv1"
    }
  ]
}
```

---

### 5.2 节目单信息

**接口**: `GET /api/live/epg`

**描述**: 获取频道节目单

**认证**: 无需认证（公开接口）

**请求参数**:
- `source` (query string): 直播源标识
- `tvgId` (query string): 频道tvg-id

**响应**:
```json
{
  "success": true,
  "data": {
    "tvgId": "cctv1",
    "source": "string",
    "epgUrl": "string",
    "programs": [
      {
        "start": "2023-01-01 08:00:00",
        "stop": "2023-01-01 09:00:00", 
        "title": "新闻联播",
        "desc": "节目描述"
      }
    ]
  }
}
```

---

## 6. TVBox相关接口

### 6.1 TVBox配置文件

**接口**: `GET /api/tvbox/config`

**描述**: 生成TVBox订阅配置

**认证**: 无需认证（公开接口）

**请求参数**:
- `format` (query string, 可选): 格式 `json` 或 `base64`，默认json
- `mode` (query string, 可选): 模式 `safe`、`min`、`yingshicang`、`fast`
- `forceSpiderRefresh` (query string, 可选): 强制刷新spider，值为1

**响应**:
```json
{
  "spider": "https://example.com/spider.jar;md5;hash",
  "wallpaper": "https://example.com/wallpaper.jpg",
  "sites": [
    {
      "key": "string",
      "name": "string", 
      "type": 1,
      "api": "string",
      "searchable": 1,
      "quickSearch": 1,
      "filterable": 1
    }
  ],
  "lives": [
    {
      "name": "string",
      "type": 0,
      "url": "string",
      "ua": "string",
      "epg": "string"
    }
  ],
  "parses": [
    {
      "name": "string",
      "type": 0,
      "url": "string"
    }
  ]
}
```

**特殊说明**:
- 智能选择最优Spider JAR文件
- 支持多种模式优化配置
- 自动检测API类型并优化参数

---

### 6.2 Spider健康检查

**接口**: `GET /api/tvbox/health`

**描述**: 检查Spider JAR文件可用性

**请求参数**:
- `url` (query string): JAR文件URL

**响应**:
```json
{
  "url": "string",
  "status": 200,
  "statusText": "OK",
  "accessible": true,
  "contentType": "application/java-archive",
  "contentLength": "1234567",
  "lastModified": "string",
  "timestamp": "2023-01-01T00:00:00.000Z"
}
```

---

### 6.3 Spider JAR文件

**接口**: `GET /api/spider`

**描述**: 获取Spider JAR文件

**请求参数**:
- `refresh` (query string, 可选): 强制刷新缓存，值为1

**响应**: 二进制JAR文件

**响应头**:
- `Content-Type`: `application/java-archive`
- `X-Spider-Source`: JAR文件来源URL
- `X-Spider-Success`: 是否成功获取
- `X-Spider-MD5`: 文件MD5校验值

---

## 7. 代理接口

### 7.1 M3U8代理

**接口**: `GET /api/proxy/m3u8`

**描述**: 代理M3U8直播流，解决跨域问题

**请求参数**:
- `url` (query string): 原始M3U8 URL
- `allowCORS` (query string, 可选): 是否允许CORS，值为true
- `decotv-source` (query string): 直播源标识

**响应**: 重写后的M3U8内容

**特殊说明**:
- 自动重写M3U8内容中的URL
- 处理嵌套M3U8文件
- 支持加密密钥代理

---

### 7.2 图片代理

**接口**: `GET /api/image-proxy`

**描述**: 代理图片资源，解决防盗链问题

**请求参数**:
- `url` (query string): 原始图片URL

**响应**: 图片二进制数据

**特殊说明**:
- 自动设置Referer头
- 长期缓存（6个月）
- 兼容OrionTV

---

## 8. 管理员接口

### 8.1 获取管理配置

**接口**: `GET /api/admin/config`

**描述**: 获取管理员配置信息

**认证**: 需要管理员权限

**响应**:
```json
{
  "Role": "owner|admin",
  "Config": {
    "SiteConfig": {
      "SiteName": "string",
      "Announcement": "string",
      "SearchDownstreamMaxPage": 5,
      "SiteInterfaceCacheTime": 300,
      "DoubanProxyType": "string",
      "DoubanProxy": "string",
      "DisableYellowFilter": false,
      "FluidSearch": true
    },
    "SourceConfig": [],
    "LiveConfig": [],
    "UserConfig": {
      "Users": [],
      "Tags": []
    }
  }
}
```

---

### 8.2 站点配置

**接口**: `POST /api/admin/site`

**描述**: 更新站点配置

**认证**: 需要管理员权限

**请求参数**:
```json
{
  "SiteName": "string",
  "Announcement": "string", 
  "SearchDownstreamMaxPage": 5,
  "SiteInterfaceCacheTime": 300,
  "DoubanProxyType": "string",
  "DoubanProxy": "string",
  "DoubanImageProxyType": "string",
  "DoubanImageProxy": "string",
  "DisableYellowFilter": false,
  "FluidSearch": true
}
```

---

### 8.3 用户管理

**接口**: `POST /api/admin/user`

**描述**: 管理用户和用户组

**认证**: 需要管理员权限

**请求参数**:
```json
{
  "action": "add|ban|unban|setAdmin|cancelAdmin|changePassword|deleteUser|updateUserApis|userGroup|updateUserGroups|batchUpdateUserGroups",
  "targetUsername": "string",
  "targetPassword": "string",
  "enabledApis": ["api1", "api2"],
  "userGroups": ["group1", "group2"]
}
```

**支持的操作**:
- `add`: 添加用户
- `ban`/`unban`: 封禁/解封用户  
- `setAdmin`/`cancelAdmin`: 设置/取消管理员
- `changePassword`: 修改密码
- `deleteUser`: 删除用户
- `updateUserApis`: 更新用户API权限
- `userGroup`: 用户组管理
- `updateUserGroups`: 更新用户组
- `batchUpdateUserGroups`: 批量更新用户组

---

### 8.4 视频源管理

**接口**: `POST /api/admin/source`

**描述**: 管理视频采集源

**认证**: 需要管理员权限

**请求参数**:
```json
{
  "action": "add|disable|enable|delete|sort|batch_disable|batch_enable|batch_delete",
  "key": "string",
  "name": "string",
  "api": "string", 
  "detail": "string",
  "keys": ["key1", "key2"],
  "order": ["key1", "key2", "key3"]
}
```

**支持的操作**:
- `add`: 添加视频源
- `disable`/`enable`: 禁用/启用视频源
- `delete`: 删除视频源
- `sort`: 排序视频源
- `batch_*`: 批量操作

---

### 8.5 直播源管理

**接口**: `POST /api/admin/live`

**描述**: 管理直播源

**认证**: 需要管理员权限

**请求参数**:
```json
{
  "action": "add|delete|enable|disable|edit|sort",
  "key": "string",
  "name": "string",
  "url": "string",
  "ua": "string", 
  "epg": "string",
  "order": ["key1", "key2"]
}
```

---

## 9. 系统接口

### 9.1 服务器配置

**接口**: `GET /api/server-config`

**描述**: 获取服务器基础配置信息

**响应**:
```json
{
  "SiteName": "string",
  "StorageType": "localstorage|redis|upstash|kvrocks",
  "Version": "string"
}
```

---

### 9.2 版本检查

**接口**: `GET /api/version/check`

**描述**: 检查系统版本更新

**响应**:
```json
{
  "success": true,
  "current": {
    "version": "string",
    "timestamp": 1234567890
  },
  "hasUpdate": false,
  "remote": {
    "version": "string", 
    "timestamp": 1234567890
  },
  "timestamp": 1234567890
}
```

---

## 错误码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/认证失败 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 权限等级

| 角色 | 权限说明 |
|------|----------|
| owner | 站长，拥有所有权限 |
| admin | 管理员，可管理用户和配置（除站长账户外） |
| user | 普通用户，仅可访问基础功能 |

## 缓存策略

- **搜索接口**: 根据配置的缓存时间进行CDN缓存
- **豆瓣数据**: 长期缓存，提升访问速度
- **静态资源**: 长期缓存（6个月）
- **管理配置**: 不缓存，确保实时性

## 安全特性

1. **HMAC签名验证**: 防止cookie伪造
2. **角色权限控制**: 严格的权限分级
3. **输入验证**: 所有用户输入都进行验证
4. **SQL注入防护**: 使用参数化查询
5. **CORS控制**: 严格的跨域访问控制

---

*文档生成时间: ${new Date().toISOString()}*
*API版本: 基于源码分析生成*