package com.hienao.scorpiotv.presentation.screen.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.hienao.scorpiotv.domain.model.LiveChannel
import com.hienao.scorpiotv.presentation.screen.live.LiveContract
import org.koin.androidx.compose.koinViewModel

/**
 * 直播页面
 */
@Composable
fun LiveScreen(
    viewModel: LiveViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.handleIntent(LiveContract.UiIntent.LoadChannels)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 分类标签栏
        CategoryTabs(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category: String ->
                viewModel.handleIntent(LiveContract.UiIntent.SelectCategory(category))
            }
        )
        
        // 频道列表
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
                Text(
                    text = "加载中...",
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(uiState.filteredChannels) { channel ->
                    LiveChannelItem(
                        channel = channel,
                        onChannelClick = {
                            viewModel.handleIntent(LiveContract.UiIntent.PlayChannel(channel))
                        }
                    )
                }
            }
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // 刷新指示器
        if (uiState.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                selected = category == selectedCategory
            )
        }
    }
}

@Composable
fun LiveChannelItem(
    channel: LiveChannel,
    onChannelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onChannelClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 频道信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = channel.desc,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 观看人数
                    Text(
                        text = channel.viewers,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    
                    // 状态指示
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (channel.status == "online") 
                                    Color.Green 
                                else 
                                    Color.Red
                            )
                    )
                }
            }
            
            // 类别标签
            Text(
                text = channel.category,
                color = Color.Blue,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(
                        Color.DarkGray,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LiveScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            // LiveScreen() // 预览中暂时注释掉，避免ViewModel依赖问题
        }
    }
}