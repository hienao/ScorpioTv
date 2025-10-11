package com.hienao.scorpiotv.di

import com.hienao.scorpiotv.data.network.ApiService
import com.hienao.scorpiotv.data.network.KtorClient
import com.hienao.scorpiotv.data.network.NetworkClient
import com.hienao.scorpiotv.data.repository.MediaRepositoryImpl
import com.hienao.scorpiotv.domain.repository.MediaRepository
import com.hienao.scorpiotv.presentation.screen.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin依赖注入模块
 * 定义应用程序的依赖关系
 */

val dataModule = module {
    // 提供IO调度器
    single { Dispatchers.IO }
    
    // 仓库实现
    single<MediaRepository> {
        MediaRepositoryImpl(
            dispatcher = get(),
            // 这里可以添加其他依赖，如数据源
        )
    }
}

val viewModelModule = module {
    // ViewModel定义
    viewModel { HomeViewModel(get()) }
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
    single { KtorClient(get()) }
    
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