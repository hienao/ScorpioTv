package com.hienao.scorpiotv.presentation.screen.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 电影页面筛选组件
 */
@Composable
fun MovieFilterSection(
    filterState: com.hienao.scorpiotv.domain.model.MovieFilterState,
    onPrimaryCategorySelected: (String) -> Unit,
    onFilterTypeSelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onRegionSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 第一行分类筛选
        PrimaryCategoryFilterRow(
            categories = filterState.primaryCategories,
            selectedCategory = filterState.primaryCategory,
            onCategorySelected = onPrimaryCategorySelected
        )
        
        // 第二行条件筛选
        SecondaryFilterRow(
            filterTypes = filterState.filterTypes,
            selectedFilterType = filterState.filterType,
            onFilterTypeSelected = onFilterTypeSelected,
            filterState = filterState,
            onTypeSelected = onTypeSelected,
            onRegionSelected = onRegionSelected,
            onSortSelected = onSortSelected
        )
    }
}

/**
 * 第一行分类筛选
 */
@Composable
private fun PrimaryCategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类标签
        Text(
            text = "分类",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 分类选项
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                FilterChip(
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = category == selectedCategory
                )
            }
        }
    }
}

/**
 * 第二行条件筛选 - 类型/地区/排序
 */
@Composable
private fun SecondaryFilterRow(
    filterTypes: List<String>,
    selectedFilterType: String,
    onFilterTypeSelected: (String) -> Unit,
    filterState: com.hienao.scorpiotv.domain.model.MovieFilterState,
    onTypeSelected: (String) -> Unit,
    onRegionSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 筛选类型选择行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 筛选标签
            Text(
                text = "筛选",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 筛选类型选项
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filterTypes) { filterType ->
                    FilterChip(
                        onClick = { onFilterTypeSelected(filterType) },
                        label = {
                            Text(
                                text = filterType,
                                fontSize = 14.sp,
                                fontWeight = if (filterType == selectedFilterType) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = filterType == selectedFilterType
                    )
                }
            }
        }
        
        // 具体筛选选项
        when (selectedFilterType) {
            "类型" -> {
                if (filterState.typeOptions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = 80.dp, end = 16.dp) // 与筛选标签对齐
                    ) {
                        items(filterState.typeOptions) { type ->
                            FilterChip(
                                onClick = { onTypeSelected(type) },
                                label = {
                                    Text(
                                        text = type,
                                        fontSize = 14.sp
                                    )
                                },
                                selected = type == filterState.selectedType
                            )
                        }
                    }
                }
            }
            "地区" -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 80.dp, end = 16.dp) // 与筛选标签对齐
                ) {
                    items(filterState.regionOptions) { region ->
                        FilterChip(
                            onClick = { onRegionSelected(region) },
                            label = {
                                Text(
                                    text = region,
                                    fontSize = 14.sp
                                )
                            },
                            selected = region == filterState.selectedRegion
                        )
                    }
                }
            }
            "排序" -> {
                if (filterState.sortOptions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = 80.dp, end = 16.dp) // 与筛选标签对齐
                    ) {
                        items(filterState.sortOptions) { sort ->
                            FilterChip(
                                onClick = { onSortSelected(sort) },
                                label = {
                                    Text(
                                        text = sort,
                                        fontSize = 14.sp
                                    )
                                },
                                selected = sort == filterState.selectedSort
                            )
                        }
                    }
                }
            }
        }
    }
}
