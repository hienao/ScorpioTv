package com.hienao.scorpiotv.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import coil.ImageLoader
import com.hienao.scorpiotv.ScorpioTvApplication
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.data.network.DoubanApiService
import com.hienao.scorpiotv.data.network.KtorClient
import com.hienao.scorpiotv.data.network.NetworkClient
import com.hienao.scorpiotv.data.repository.DoubanRepositoryImpl
import com.hienao.scorpiotv.data.repository.MediaRepositoryImpl
import com.hienao.scorpiotv.data.repository.UserRepositoryImpl
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import com.hienao.scorpiotv.domain.repository.MediaRepository
import com.hienao.scorpiotv.domain.repository.UserRepository
import com.hienao.scorpiotv.presentation.screen.home.HomeViewModel
import com.hienao.scorpiotv.ui.image.CoilImageLoader
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

// DataStore扩展
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Koin依赖注入模块
 * 定义应用程序的依赖关系
 */

val dataModule = module {
    // 提供IO调度器 - 同时提供两种类型
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<CoroutineContext> { Dispatchers.IO }
    
    // DataStore
    single<DataStore<Preferences>> { get<Context>().dataStore }
    
    // 仓库实现
    single<MediaRepository> {
        MediaRepositoryImpl(
            dispatcher = get(),
            // 这里可以添加其他依赖，如数据源
        )
    }
    
    // 用户仓库实现
    single<UserRepository> {
        UserRepositoryImpl(
            apiService = get(),
            dataStore = get(),
            dispatcher = get()
        )
    }
    
    // 豆瓣仓库实现 - 添加UserRepository依赖
    single<DoubanRepository> {
        DoubanRepositoryImpl(
            apiService = get(),
            doubanApiService = get(),
            userRepository = get(),
            ioDispatcher = get()
        )
    }
}

val viewModelModule = module {
    // ViewModel定义
    viewModel { HomeViewModel(get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.login.LoginViewModel(get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.main.MainViewModel(get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.media.MediaViewModel(get(), get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.player.PlayerViewModel(get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.settings.SettingsViewModel(get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.live.LiveViewModel(get()) }
    viewModel { com.hienao.scorpiotv.presentation.screen.search.SearchViewModel(get()) }
}

val useCaseModule = module {
    // 用例定义
    // factory { LoadMediaUseCase(get()) }
    // factory { SaveUserPreferencesUseCase(get()) }
}

val networkModule = module {
    // 需要认证的HttpClient - 用于登录、搜索等需要认证的接口
    single<HttpClient>(named("auth")) {
        NetworkClient.createHttpClient(
            enableLogging = true,
            timeout = 30_000L,
            storeCookies = true
        )
    }
    
    // 公开API的HttpClient - 不携带Cookie，用于豆瓣接口等公开API
    single<HttpClient>(named("public")) {
        NetworkClient.createHttpClient(
            enableLogging = true,
            timeout = 30_000L,
            storeCookies = false
        )
    }
    
    // 默认HttpClient - 使用认证版本，保持向后兼容
    single<HttpClient> { get<HttpClient>(named("auth")) }
    
    // KtorClient 封装 - 使用认证版本
    single { KtorClient(get()) }
    
    // 认证API的KtorClient - 携带Cookie
    single<KtorClient>(named("auth")) { KtorClient(get(named("auth"))) }
    
    // 公开API的KtorClient - 不携带Cookie
    single<KtorClient>(named("public")) { KtorClient(get(named("public"))) }
    
    // API Service - 使用认证版本
    single { ApiService(get()) }
    
    // 豆瓣API Service - 使用认证版本，携带Cookie（根据API文档，豆瓣接口需要认证）
    single { DoubanApiService(get(named("auth"))) }
    
    // Coil ImageLoader 配置
    single<ImageLoader> {
        CoilImageLoader.createImageLoader(
            context = get()
        )
    }
}

val databaseModule = module {
    // 数据库相关依赖
    // single { provideDatabase(androidContext()) }
    // single { provideMediaDao(get()) }
}

/**
 * 所有模块的集合
 */
val appModules = listOf(
    dataModule,
    viewModelModule,
    useCaseModule,
    networkModule,
    databaseModule
)