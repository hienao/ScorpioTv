package com.hienao.scorpiotv.ui.image

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.ErrorResult
import com.hienao.scorpiotv.R
import com.hienao.scorpiotv.data.network.NetworkClient
import com.hienao.scorpiotv.data.network.NetworkDebugLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Coil 图片加载器配置
 * 提供统一的图片加载配置，包括网络客户端、缓存策略和错误处理
 */
object CoilImageLoader {
    
    /**
     * 创建配置好的 ImageLoader 实例
     * @param context Android 上下文
     * @return 配置好的 ImageLoader
     */
    fun createImageLoader(
        context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // 内存缓存配置
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 使用 25% 的可用内存
                    .build()
            }
            // 磁盘缓存配置
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512MB
                    .build()
            }
            // 网络请求配置 - 使用 OkHttp
            .okHttpClient { createOkHttpClient() }
            // 解码器配置 - 支持 GIF 和其他格式
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            // 缓存策略
            .respectCacheHeaders(false) // 忽略服务器缓存头，使用我们自己的策略
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // 错误处理
            .error(R.drawable.ic_launcher_background) // 默认错误图片
            .placeholder(R.drawable.ic_launcher_background) // 占位图
            .build()
    }
    
    /**
     * 创建 OkHttpClient 用于图片加载
     * 配置超时和重试策略
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                
                // 添加 User-Agent 头
                val newRequest = request.newBuilder()
                    .header("User-Agent", "ScorpioTv/1.0 (Android)")
                    .build()
                
                try {
                    val response = chain.proceed(newRequest)
                    
                    // 记录响应状态
                    Log.d("CoilNetwork", "图片请求响应: $url - ${response.code}")
                    
                    if (!response.isSuccessful) {
                        Log.w("CoilNetwork", "图片请求失败: $url - ${response.code} ${response.message}")
                    }
                    
                    response
                } catch (e: Exception) {
                    Log.e("CoilNetwork", "图片请求异常: $url", e)
                    throw e
                }
            }
            .build()
    }
    
    /**
     * 创建带有自定义配置的 ImageRequest
     * @param context Android 上下文
     * @param imageUrl 图片 URL
     * @param imageLoader ImageLoader 实例
     * @param placeholder 占位图资源 ID
     * @param error 错误图资源 ID
     * @param crossfade 是否启用淡入淡出效果
     * @return 配置好的 ImageRequest
     */
    fun createImageRequest(
        context: Context,
        imageUrl: String,
        imageLoader: ImageLoader,
        placeholder: Int = R.drawable.ic_launcher_background,
        error: Int = R.drawable.ic_launcher_background,
        crossfade: Boolean = true
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .placeholder(placeholder)
            .error(error)
            .crossfade(crossfade)
            .target(
                onStart = { placeholder ->
                    Log.d("CoilImageLoader", "开始加载图片: $imageUrl")
                },
                onSuccess = { result ->
                    Log.d("CoilImageLoader", "图片加载成功: $imageUrl")
                },
                onError = { errorResult ->
                    Log.e("CoilImageLoader", "图片加载失败: $imageUrl")
                }
            )
            .build()
    }
    
    /**
     * 验证图片 URL 是否有效
     * @param url 图片 URL
     * @return URL 是否有效
     */
    fun isValidImageUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            Log.w("CoilImageLoader", "图片 URL 为空")
            return false
        }
        
        // 检查 URL 格式
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Log.w("CoilImageLoader", "图片 URL 格式无效: $url")
            return false
        }
        
        // 检查是否包含常见的图片文件扩展名
        val imageExtensions = setOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")
        val hasValidExtension = imageExtensions.any { ext ->
            url.lowercase().endsWith(ext)
        }
        
        // 如果没有明确的扩展名，仍然尝试加载（可能是动态生成的 URL）
        if (!hasValidExtension) {
            Log.d("CoilImageLoader", "图片 URL 没有明确的扩展名，但仍尝试加载: $url")
        }
        
        return true
    }
}