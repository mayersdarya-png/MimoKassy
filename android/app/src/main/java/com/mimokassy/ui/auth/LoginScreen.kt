package com.mimokassy.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.api.RequestCodeRequest
import com.mimokassy.ui.theme.CoolBlue
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val isPhoneValid = phone
        .replace("[^0-9+]".toRegex(), "")
        .let { it.length >= 11 && it.startsWith("+") }

    Scaffold(
        containerColor = Color.White
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

            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = "Логотип",
                tint = Color(0xFF1A1A1A),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Мимо Кассы",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "Запись на кулинарные классы",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6C6C6C)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Телефон",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6C6C6C)
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = if (!isPhoneValid && phone.isNotEmpty()) {
                            "Похоже, номер введён не полностью"
                        } else null
                    },
                    placeholder = { Text("+7 999 123-45-67") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = phoneError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CoolBlue.copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = CoolBlue,
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        errorIndicatorColor = Color(0xFFFF3B30)
                    )
                )

                if (phoneError != null) {
                    Text(
                        text = phoneError!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF3B30)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (!isPhoneValid) {
                            phoneError = "Введите корректный номер"
                            return@launch
                        }
                        isLoading = true
                        phoneError = null
                        try {
                            RetrofitInstance.authApi.requestCode(RequestCodeRequest(phone = phone))
                            // ✅ ПЕРЕХОД НА ЭКРАН OTP
                            navController.navigate("otp/$phone")
                        } catch (e: Exception) {
                            phoneError = e.message ?: "Ошибка отправки кода"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = isPhoneValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPhoneValid && !isLoading) CoolBlue else Color.Gray,
                    contentColor = if (isPhoneValid && !isLoading) Color(0xFF1A1A1A) else Color.White
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
                        text = "Получить код",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Без пароля — входим по номеру телефона",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6C6C6C)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}