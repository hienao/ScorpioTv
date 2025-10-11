package com.hienao.scorpiotv.presentation.base

import androidx.compose.runtime.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * MVI架构基础Screen Composable
 * 提供通用的效果处理和状态收集逻辑
 */
@Composable
fun <S : BaseContract.UiState, I : BaseContract.UiIntent, E : BaseContract.UiEffect> BaseScreen(
    uiState: StateFlow<S>,
    uiEffect: Flow<E>,
    onIntent: (I) -> Unit,
    content: @Composable (state: S, onIntent: (I) -> Unit) -> Unit
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val state by uiState.collectAsState()
    
    // 处理UI效果
    LaunchedEffect(uiEffect, lifecycleOwner.lifecycle) {
        uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                // 这里可以添加通用的效果处理逻辑
                // 具体的效果处理由子类实现
            }
    }
    
    content(state, onIntent)
}

/**
 * 收集UI效果的扩展函数
 * 用于在Composable中处理一次性事件
 */
@Composable
fun <E : BaseContract.UiEffect> Flow<E>.CollectAsEffect(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: (E) -> Unit
) {
    LaunchedEffect(this, lifecycle) {
        flowWithLifecycle(lifecycle, minActiveState).collect { effect ->
            onEffect(effect)
        }
    }
}

/**
 * 收集UI效果的另一种实现，使用SharedFlow
 */
@Composable
fun <E : BaseContract.UiEffect> SharedFlow<E>.CollectAsEffect(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: (E) -> Unit
) {
    LaunchedEffect(this, lifecycle) {
        flowWithLifecycle(lifecycle, minActiveState).collect { effect ->
            onEffect(effect)
        }
    }
}

/**
 * 在LifecycleScope中启动协程的扩展函数
 */
fun LifecycleOwner.launchWhenStarted(block: suspend () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            block()
        }
    }
}

/**
 * 在LifecycleScope中启动协程的扩展函数
 */
fun LifecycleOwner.launchWhenResumed(block: suspend () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block()
        }
    }
}