package com.hienao.scorpiotv

import android.app.Application
import com.hienao.scorpiotv.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * ScorpioTv应用程序类
 * 负责初始化Koin依赖注入框架
 */
class ScorpioTvApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Koin
        startKoin {
            // 启用Android日志
            androidLogger()
            
            // 提供Android上下文
            androidContext(this@ScorpioTvApplication)
            
            // 加载所有模块
            modules(appModules)
        }
    }
}