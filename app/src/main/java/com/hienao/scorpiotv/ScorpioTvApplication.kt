package com.hienao.scorpiotv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.hienao.scorpiotv.di.appModules
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * ScorpioTv应用程序类
 * 负责初始化Koin依赖注入框架和Coil图片加载器
 */
class ScorpioTvApplication : Application(), ImageLoaderFactory {
    
    // 注入ImageLoader实例
    private val imageLoader: ImageLoader by inject()
    
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
        
        // 初始化Coil图片加载器
        // 通过实现ImageLoaderFactory接口，Coil会自动使用我们配置的ImageLoader
        android.util.Log.d("ScorpioTvApp", "Coil图片加载器已初始化")
    }
    
    /**
     * 实现ImageLoaderFactory接口
     * 提供全局的ImageLoader实例
     */
    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
}