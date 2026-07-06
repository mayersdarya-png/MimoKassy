package com.mimokassy.ui.auth

import androidx.compose.ui.text.style.TextAlign

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.api.VerifyCodeRequest
import com.mimokassy.data.api.RequestCodeRequest
import com.mimokassy.ui.theme.CoolBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPScreen(
    navController: NavController,
    phone: String
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val codeLength = 6
    var code by remember { mutableStateOf(List(codeLength) { "" }) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var canResend by remember { mutableStateOf(false) }
    var resendTimer by remember { mutableStateOf(60) }
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }

    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
        canResend = true
    }

    fun onCodeChanged(index: Int, value: String) {
        val newCode = code.toMutableList()
        newCode[index] = value.takeLast(1)
        code = newCode

        if (value.isNotEmpty() && index < codeLength - 1) {
            focusRequesters[index + 1].requestFocus()
        }

        if (code.all { it.isNotEmpty() }) {
            val fullCode = code.joinToString("")
            scope.launch {
                try {
                    isLoading = true
                    errorMessage = null
                    val response = RetrofitInstance.authApi.verifyCode(
                        VerifyCodeRequest(phone = phone, code = fullCode)
                    )
                    // ✅ СОХРАНЯЕМ ТОКЕН
                    val sharedPref = context.getSharedPreferences("mimo_kassy", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("access_token", response.tokens.access_token).apply()

                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Неверный код"
                    code = List(codeLength) { "" }
                    focusRequesters.first().requestFocus()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Подтверждение") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Мы отправили код на",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6C6C6C)
            )

            Text(
                text = phone,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ============================================
            // 6 отдельных ячеек для кода
            // ============================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until codeLength) {
                    TextField(
                        value = code[i],
                        onValueChange = { onCodeChanged(i, it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .focusRequester(focusRequesters[i]),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CoolBlue.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = CoolBlue,
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            errorIndicatorColor = Color(0xFFFF3B30)
                        ),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp
                        ),
                        isError = errorMessage != null
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF3B30),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        val fullCode = code.joinToString("")
                        try {
                            isLoading = true
                            errorMessage = null
                            val response = RetrofitInstance.authApi.verifyCode(
                                VerifyCodeRequest(phone = phone, code = fullCode)
                            )
                            val sharedPref = context.getSharedPreferences("mimo_kassy", Context.MODE_PRIVATE)
                            sharedPref.edit().putString("access_token", response.tokens.access_token).apply()

                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Неверный код"
                            code = List(codeLength) { "" }
                            focusRequesters.first().requestFocus()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = code.all { it.isNotEmpty() } && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (code.all { it.isNotEmpty() } && !isLoading) CoolBlue else Color.Gray,
                    contentColor = if (code.all { it.isNotEmpty() } && !isLoading) Color(0xFF1A1A1A) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Подтвердить",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (canResend) {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                RetrofitInstance.authApi.requestCode(
                                    RequestCodeRequest(phone = phone)
                                )
                                canResend = false
                                resendTimer = 60
                                while (resendTimer > 0) {
                                    delay(1000)
                                    resendTimer--
                                }
                                canResend = true
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Отправить код повторно",
                        color = Color(0xFF1A1A1A)
                    )
                }
            } else {
                Text(
                    text = "Отправить код повторно (${resendTimer}s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6C6C6C)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text(
                    text = "Изменить номер",
                    color = Color(0xFF6C6C6C)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}