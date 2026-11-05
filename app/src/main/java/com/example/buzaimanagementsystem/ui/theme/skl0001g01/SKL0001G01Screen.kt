package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SKL0001G01Screen(
    viewModel: SKL0001G01ViewModel = viewModel(),
    onCloseApp: () -> Unit,
    onLoginSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit
) {
    var rawInputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val isAndroid12Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val requiredPermissions = remember {
        mutableListOf<String>().apply {
            if (isAndroid12Plus) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toTypedArray()
    }

    var hasAllPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAllPermissions = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        viewModel.setLoginSuccessCallback { uCd, userName, gymTntCd, gymTntMei ->
            onLoginSuccess(uCd, userName, gymTntCd, gymTntMei)
        }
        viewModel.checkSystemAvailability()
        if (!hasAllPermissions && requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = rawInputText,
                onValueChange = { newValue ->
                    rawInputText = newValue
                    if (newValue.contains("\n") || newValue.contains("\r")) {
                        val textToProcess = rawInputText.trim()
                        rawInputText = ""
                        if (viewModel.isSystemAvailable && !viewModel.isLoggingIn && textToProcess.isNotEmpty()) {
                            viewModel.executeLogin(textToProcess) { uCd, userName, gymTntCd, gymTntMei ->
                                onLoginSuccess(uCd, userName, gymTntCd, gymTntMei)
                            }
                        }
                    }
                },
                modifier = Modifier.size(1.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val textToProcess = rawInputText.trim()
                        rawInputText = ""
                        if (viewModel.isSystemAvailable && !viewModel.isLoggingIn && textToProcess.isNotEmpty()) {
                            viewModel.executeLogin(textToProcess) { uCd, userName, gymTntCd, gymTntMei ->
                                onLoginSuccess(uCd, userName, gymTntCd, gymTntMei)
                            }
                        }
                    }
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "部材所在管理システム",
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                        .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "ユーザーIDのQRコードを読み込んでください",
                        fontSize = 14.sp,
                        color = if (viewModel.errorMessage != null) MaterialTheme.colorScheme.error else Color.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "ユーザーID", fontSize = 16.sp, modifier = Modifier.padding(end = 16.dp))

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                            .background(Color(0xFFD4E4F7), shape = MaterialTheme.shapes.small)
                            .border(1.dp, Color.Black, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = viewModel.inputUserCd,
                            fontSize = 16.sp,
                            color = if (viewModel.inputUserCd.isEmpty()) Color.Gray else Color.Black,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.disconnectBleDevice()
                        onCloseApp()
                    },
                    modifier = Modifier.width(130.dp).height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "閉じる", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "利用可能時間", fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                    Text(text = viewModel.displayTime, fontSize = 20.sp)
                }
            }
        }
    }
}