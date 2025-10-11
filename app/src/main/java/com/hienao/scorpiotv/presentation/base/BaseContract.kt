package com.hienao.scorpiotv.presentation.base

/**
 * MVI架构基础Contract接口
 * 定义了UI状态、意图和效果的基础结构
 */
interface BaseContract {
    
    /**
     * UI状态接口
     * 所有UI状态必须实现此接口
     */
    interface UiState
    
    /**
     * UI意图接口
     * 所有用户意图必须实现此接口
     */
    interface UiIntent
    
    /**
     * UI效果接口
     * 用于一次性事件处理，如导航、Toast等
     */
    interface UiEffect
}