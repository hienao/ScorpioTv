package com.hienao.scorpiotv.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import com.hienao.scorpiotv.ScorpioTvApplication
import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.data.network.NetworkClient
import com.hienao.scorpiotv.data.repository.DoubanRepositoryImpl
import com.hienao.scorpiotv.data.repository.MediaRepositoryImpl
import com.hienao.scorpiotv.data.repository.UserRepositoryImpl
import com.hienao.scorpiotv.domain.repository.DoubanRepository
import com.hienao.scorpiotv.domain.repository.MediaRepository
import com.hienao.scorpiotv.domain.repository.UserRepository
import com.hienao.scorpiotv.presentation.screen.home.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
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
    
    // 豆瓣仓库实现
    single<DoubanRepository> {
        DoubanRepositoryImpl(
            apiService = get(),
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
    // Ktor HttpClient
    single {
        NetworkClient.createHttpClient(
            enableLogging = true,
            timeout = 30_000L
        )
    }
    
    // KtorClient 封装
    single { com.hienao.scorpiotv.data.network.KtorClient(get()) }
    
    // API Service
    single { ApiService(get()) }
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