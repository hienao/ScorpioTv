# MVI架构约束规则 - ScorpioTv项目

## 概述

本文档定义了ScorpioTv项目采用MVI（Model-View-Intent）架构和Jetpack Compose的约束规则，基于Google官方最佳实践。

## 架构原则

### 1. 单向数据流 (UDF)
- **数据流向**: Intent → ViewModel → State → UI
- **事件流向**: UI → Intent → ViewModel → State → UI
- 严格禁止双向数据绑定和直接状态修改

### 2. 响应式编程
- 使用Kotlin Coroutines和Flow处理异步操作
- StateFlow和SharedFlow作为状态和事件的主要载体
- 避免回调地狱，优先使用协程

### 3. 不可变状态
- 所有状态对象必须是不可变的
- 使用`data class`定义状态
- 状态更新通过`copy()`方法创建新实例

## 项目结构约束

### 包结构规范
```
com.hienao.scorpiotv/
├── presentation/           # UI层
│   ├── theme/             # Compose主题
│   ├── components/        # 可复用UI组件
│   ├── screen/            # 各个页面
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeContract.kt
│   │   └── ...
│   └── navigation/        # 导航配置
├── domain/               # 业务逻辑层
│   ├── model/           # 领域模型
│   ├── repository/      # 仓库接口
│   └── usecase/         # 用例
├── data/                # 数据层
│   ├── repository/      # 仓库实现
│   ├── datasource/      # 数据源
│   ├── network/         # 网络层
│   │   ├── ApiResult.kt    # 网络请求结果封装
│   │   ├── NetworkClient.kt # Ktor客户端配置
│   │   └── ApiService.kt    # API服务定义
│   └── mapper/          # 数据映射
└── di/                  # 依赖注入
```

### 命名约定
- **Screen**: `[Feature]Screen.kt`
- **ViewModel**: `[Feature]ViewModel.kt`
- **Contract**: `[Feature]Contract.kt`
- **State**: `[Feature]UiState`
- **Intent**: `[Feature]UiIntent`
- **Effect**: `[Feature]UiEffect`

## MVI实现规范

### 1. Contract定义
每个功能模块必须定义Contract接口：

```kotlin
interface HomeContract {
    data class UiState(
        val isLoading: Boolean = false,
        val items: List<MediaItem> = emptyList(),
        val error: String? = null
    )
    
    sealed class UiIntent {
        object LoadData : UiIntent()
        data class RefreshData(val force: Boolean) : UiIntent()
        data class SelectItem(val itemId: String) : UiIntent()
    }
    
    sealed class UiEffect {
        data class ShowToast(val message: String) : UiEffect()
        data class NavigateToDetail(val itemId: String) : UiEffect()
    }
}
```

### 2. ViewModel实现规范
```kotlin
class HomeViewModel(
    private val loadMediaUseCase: LoadMediaUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeContract.UiState())
    val uiState: StateFlow<HomeContract.UiState> = _uiState.asStateFlow()
    
    private val _uiEffect = MutableSharedFlow<HomeContract.UiEffect>()
    val uiEffect: SharedFlow<HomeContract.UiEffect> = _uiEffect.asSharedFlow()
    
    fun handleIntent(intent: HomeContract.UiIntent) {
        viewModelScope.launch {
            when (intent) {
                is HomeContract.UiIntent.LoadData -> loadData()
                is HomeContract.UiIntent.RefreshData -> refreshData(intent.force)
                is HomeContract.UiIntent.SelectItem -> selectItem(intent.itemId)
            }
        }
    }
}
```

### 3. Screen实现规范
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEffect by viewModel.uiEffect.collectAsInitialEffect()
    
    LaunchedEffect(Unit) {
        viewModel.handleIntent(HomeContract.UiIntent.LoadData)
    }
    
    uiEffect?.let { effect ->
        handleEffect(effect)
    }
    
    HomeContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent
    )
}
```

## Jetpack Compose约束

### 1. 组件设计原则
- **单一职责**: 每个组件只负责一个功能
- **可组合性**: 组件应该易于组合和重用
- **状态提升**: 状态尽可能提升到最近的共同祖先
- **无副作用**: Composable函数应该是无副作用的

### 2. 状态管理
```kotlin
// 错误示例 - 在Composable中直接修改状态
@Composable
fun BadCounter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { // 直接修改
        Text("Count: $count")
    }
}

// 正确示例 - 通过Intent处理状态变化
@Composable
fun GoodCounter(
    count: Int,
    onIncrement: () -> Unit
) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}
```

### 3. 预览函数
每个Screen和重要组件必须提供预览函数：

```kotlin
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ScorpioTvTheme {
        HomeScreen(
            uiState = HomeContract.UiState(
                items = sampleMediaItems
            ),
            onIntent = {}
        )
    }
}
```

## 依赖注入规范

### 1. Koin使用
- 使用Koin进行依赖注入
- 在Application中初始化Koin
- 使用`by inject()`或`get()`获取依赖
- ViewModel使用`koinViewModel()`获取实例

### 2. 模块组织
```kotlin
val dataModule = module {
    single<MediaRepository> {
        MediaRepositoryImpl(
            dispatcher = get(),
            localDataSource = get(),
            remoteDataSource = get()
        )
    }
    
    single<LocalDataSource> { LocalDataSourceImpl(get()) }
    single<RemoteDataSource> { RemoteDataSourceImpl(get()) }
    
    single { Dispatchers.IO }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

val useCaseModule = module {
    factory { LoadMediaUseCase(get()) }
    factory { SaveUserPreferencesUseCase(get()) }
}
```

### 3. Application初始化
```kotlin
class ScorpioTvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@ScorpioTvApplication)
            modules(
                dataModule,
                viewModelModule,
                useCaseModule
            )
        }
    }
}
```

## 错误处理约束

### 1. 异常处理策略
- 在ViewModel层统一处理业务异常
- 使用Result类型包装可能失败的操作
- UI层只负责显示错误状态，不处理异常逻辑

### 2. 错误状态管理
```kotlin
data class UiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: ErrorState? = null
)

sealed class ErrorState {
    data class NetworkError(val message: String) : ErrorState()
    data class ValidationError(val field: String) : ErrorState()
    object UnknownError : ErrorState()
}
```

## 测试约束

### 1. 单元测试
- 每个ViewModel必须编写单元测试
- 使用MockK模拟依赖
- 测试覆盖率不低于80%

### 2. UI测试
- 重要用户流程必须编写UI测试
- 使用ComposeTestRule进行Composable测试
- 避免测试实现细节，专注用户行为

## 性能约束

### 1. Compose性能
- 使用`remember`缓存计算结果
- 避免不必要的重组
- 使用`key`函数优化列表性能

### 2. 内存管理
- 及时取消协程避免内存泄漏
- 使用`viewModelScope`管理ViewModel协程
- 避免在Composable中持有长生命周期对象

## 代码质量约束

### 1. Kotlin编码规范
- 遵循Kotlin官方编码约定
- 使用严格的lint规则
- 禁止使用!!操作符

### 2. 注释规范
- 每个公共函数必须有KDoc注释
- 复杂业务逻辑必须添加注释说明
- 避免过时的注释信息

## 版本控制约束

### 1. 提交规范
```
feat: 添加新功能
fix: 修复bug
docs: 更新文档
style: 代码格式调整
refactor: 代码重构
test: 添加测试
chore: 构建过程或辅助工具的变动
```

### 2. 分支策略
- `main`: 生产环境分支
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 热修复分支

## 网络层规范

### 1. 网络请求封装
项目使用 Ktor Client 作为网络请求库,配合 Kotlin 协程实现异步网络请求。

#### ApiResult 封装
所有网络请求必须返回 `ApiResult<T>` 类型:

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val exception: Throwable? = null,
        val message: String? = null,
        val code: Int? = null
    ) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}
```

#### 网络客户端配置
使用 `NetworkClient` 创建配置好的 Ktor HttpClient:

```kotlin
val httpClient = NetworkClient.createHttpClient(
    enableLogging = true,
    timeout = 30_000L
)
```

### 2. API服务定义
使用 `KtorClient` 封装网络请求:

```kotlin
class ApiService(private val ktorClient: KtorClient) {
    suspend fun getMediaList(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<MediaListResponse> {
        return ktorClient.get(
            url = "$BASE_URL/media/list",
            params = mapOf(
                "page" to page.toString(),
                "pageSize" to pageSize.toString()
            )
        )
    }
}
```

### 3. 数据模型定义
使用 Kotlinx Serialization 进行 JSON 序列化:

```kotlin
@Serializable
data class MediaListResponse(
    val total: Int,
    val page: Int,
    val items: List<MediaItemDto>
)
```

### 4. 网络层依赖注入
在 Koin 模块中配置网络层依赖:

```kotlin
val networkModule = module {
    // Ktor HttpClient
    single {
        NetworkClient.createHttpClient(
            enableLogging = true,
            timeout = 30_000L
        )
    }
    
    // KtorClient 封装
    single { KtorClient(get()) }
    
    // API Service
    single { ApiService(get()) }
}
```

### 5. Repository层使用示例
在 Repository 中使用 ApiService:

```kotlin
class MediaRepositoryImpl(
    private val apiService: ApiService,
    private val dispatcher: CoroutineDispatcher
) : MediaRepository {
    override suspend fun getMediaList(page: Int): Flow<ApiResult<List<MediaItem>>> = flow {
        emit(ApiResult.Loading)
        
        val result = apiService.getMediaList(page)
        
        result.onSuccess { response ->
            val mediaItems = response.items.map { it.toDomainModel() }
            emit(ApiResult.Success(mediaItems))
        }.onError { exception, message, code ->
            emit(ApiResult.Error(exception, message, code))
        }
    }.flowOn(dispatcher)
}
```

## 工具和库约束

### 1. 必需依赖
- Jetpack Compose BOM
- Koin for dependency injection
- Kotlin Coroutines
- Navigation Compose
- Room for local storage
- Ktor Client for network requests
- Kotlinx Serialization for JSON parsing

### 2. 推荐依赖
- Coil for image loading
- MockK for testing
- Turbine for Flow testing

## 审查清单

在代码审查时，请检查以下项目：

- [ ] 是否遵循MVI架构模式
- [ ] 状态是否是不可变的
- [ ] 是否使用单向数据流
- [ ] Composable函数是否无副作用
- [ ] 是否有适当的错误处理
- [ ] 是否编写了必要的测试
- [ ] 是否遵循命名约定
- [ ] 是否有内存泄漏风险
- [ ] 是否符合性能要求
- [ ] 是否有适当的文档注释

---

**注意**: 本文档是活的文档，会随着项目发展和团队经验积累而更新。所有团队成员都有责任遵循这些约束规则，并提出改进建议。