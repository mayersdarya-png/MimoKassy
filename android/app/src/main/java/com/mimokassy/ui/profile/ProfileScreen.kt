package com.mimokassy.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.ui.theme.CoolBlue
import com.mimokassy.ui.theme.RedBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Анна Петрова") }
    val phone = "+7 999 123-45-67"
    var editedName by remember { mutableStateOf(name) }

    var showSupportDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Профиль") },
                // ✅ Кнопка "Редактировать" УБРАНА из TopBar!
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ============================================
            // Блок с данными профиля
            // ============================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CoolBlue.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isEditing) {
                        // ============================================
                        // Режим редактирования
                        // ============================================
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoolBlue
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "⚠️ Номер телефона можно сменить через поддержку",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C6C6C)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                isEditing = false
                                editedName = name
                            }) {
                                Text("Отмена", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    name = editedName
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoolBlue,
                                    contentColor = Color(0xFF1A1A1A)
                                ),
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            ) {
                                Text("Сохранить")
                            }
                        }
                    } else {
                        // ============================================
                        // Режим просмотра
                        // ============================================
                        Text(
                            text = "Имя",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C6C6C)
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Телефон",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C6C6C)
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // ============================================
            // 🔥 Кнопка "Редактировать" — ПОД блоком с данными
            // ============================================
            if (!isEditing) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isEditing = true
                        editedName = name
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoolBlue,
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "Редактировать",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ============================================
            // Справочные пункты
            // ============================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CoolBlue.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    ProfileListItem(
                        icon = Icons.Filled.Description,
                        title = "Правила студии",
                        onClick = { showRulesDialog = true }
                    )
                    Divider()
                    ProfileListItem(
                        icon = Icons.Filled.Email,
                        title = "Поддержка",
                        onClick = { showSupportDialog = true }
                    )
                    Divider()
                    ProfileListItem(
                        icon = Icons.Filled.Info,
                        title = "Версия приложения",
                        subtitle = "1.0.0"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ============================================
            // Кнопки "Выйти" и "Удалить аккаунт"
            // ============================================
            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Выйти",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6C6C6C)
                )
            }

            TextButton(
                onClick = { /* Удалить аккаунт */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Удалить аккаунт",
                    style = MaterialTheme.typography.labelLarge,
                    color = RedBrown
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Диалоги без изменений
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Поддержка") },
            text = {
                Column {
                    Text("Проблемы с приложением? Хотите узнать про новые курсы? У вас родились котята?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📞 +7 (958) 509-35-60")
                    Text("📧 mayersdarya@gmail.com")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Text("Закрыть")
                }
            }
        )
    }

    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = { Text("Правила студии") },
            text = {
                Text("Не балуйтесь (по возможности)")
            },
            confirmButton = {
                TextButton(
                    onClick = { showRulesDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun ProfileListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1A1A1A)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6C6C6C)
                    )
                }
            }
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF6C6C6C)
            )
        }
    }
}
