package com.hienao.scorpiotv.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI架构基础ViewModel
 * 提供状态管理和效果处理的基础功能
 */
abstract class BaseViewModel<S : BaseContract.UiState, I : BaseContract.UiIntent, E : BaseContract.UiEffect>(
    initialState: S
) : ViewModel() {
    
    /**
     * UI状态的MutableStateFlow
     */
    private val _uiState: MutableStateFlow<S> = MutableStateFlow(initialState)
    
    /**
     * 暴露给UI的不可变StateFlow
     */
    val uiState: StateFlow<S> = _uiState.asStateFlow()
    
    /**
     * UI效果的Channel，用于一次性事件
     */
    private val _uiEffect = Channel<E>()
    
    /**
     * 暴露给UI的不可变Flow
     */
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()
    
    /**
     * 处理用户意图的抽象方法
     * 子类必须实现此方法来处理具体的业务逻辑
     */
    abstract fun handleIntent(intent: I)
    
    /**
     * 更新UI状态
     * @param reducer 状态转换函数
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.value = _uiState.value.reducer()
    }
    
    /**
     * 发送UI效果
     * @param effect 要发送的效果
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
    
    /**
     * 获取当前状态
     */
    protected val currentState: S
        get() = _uiState.value
    
    /**
     * 在viewModelScope中启动协程
     */
    protected fun launchViewModelScope(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}