package com.hienao.scorpiotv.presentation.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hienao.scorpiotv.presentation.base.CollectAsEffect
import org.koin.androidx.compose.koinViewModel

/**
 * 登录弹窗
 */
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // 处理UI效果
    viewModel.uiEffect.CollectAsEffect { effect ->
        when (effect) {
            is LoginContract.UiEffect.LoginSuccess -> {
                onLoginSuccess()
            }
            is LoginContract.UiEffect.Dismiss -> {
                onDismiss()
            }
            is LoginContract.UiEffect.ShowToast -> {
                // 这里可以显示Toast，暂时用ShowError代替
            }
            is LoginContract.UiEffect.ShowError -> {
                // 错误信息已经在UI状态中处理
            }
        }
    }

    // 初始化时检查登录状态和加载保存的服务器地址
    LaunchedEffect(Unit) {
        viewModel.checkLoginStatus()
        viewModel.loadSavedServerUrl()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LoginContent(
                uiState = uiState,
                onIntent = viewModel::handleIntent,
                onDismiss = onDismiss,
                keyboardController = keyboardController
            )
        }
    }
}

/**
 * 登录内容组件
 */
@Composable
private fun LoginContent(
    uiState: LoginContract.UiState,
    onIntent: (LoginContract.UiIntent) -> Unit,
    onDismiss: () -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "登录 ScorpioTV",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 服务器地址输入框
        OutlinedTextField(
            value = uiState.serverUrl,
            onValueChange = { onIntent(LoginContract.UiIntent.UpdateServerUrl(it)) },
            label = { Text("服务器地址") },
            placeholder = { Text("例如: http://192.168.1.100:3000") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            isError = uiState.errorMessage?.contains("服务器") == true
        )

        // 用户名输入框（可选）
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { onIntent(LoginContract.UiIntent.UpdateUsername(it)) },
            label = { Text("用户名（可选）") },
            placeholder = { Text("LocalStorage模式可留空") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            isError = uiState.errorMessage?.contains("用户名") == true
        )

        // 密码输入框
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(LoginContract.UiIntent.UpdatePassword(it)) },
            label = { Text("密码") },
            placeholder = { Text("请输入密码") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onIntent(LoginContract.UiIntent.Login)
                }
            ),
            singleLine = true,
            visualTransformation = if (uiState.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = { onIntent(LoginContract.UiIntent.TogglePasswordVisibility) }
                ) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (uiState.isPasswordVisible) {
                            "隐藏密码"
                        } else {
                            "显示密码"
                        }
                    )
                }
            },
            isError = uiState.errorMessage?.contains("密码") == true
        )

        // 错误信息显示
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 登录按钮
        Button(
            onClick = { 
                keyboardController?.hide()
                onIntent(LoginContract.UiIntent.Login)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading && uiState.serverUrl.isNotBlank() && uiState.password.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "登录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 取消按钮
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = "取消",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 登录弹窗预览函数
 */
@Preview(showBackground = true)
@Composable
private fun LoginDialogPreview() {
    MaterialTheme {
        LoginDialog(
            onDismiss = {},
            onLoginSuccess = {}
        )
    }
}