package com.mimokassy.ui.slots

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.models.*
import com.mimokassy.ui.components.EmptyStateView
import com.mimokassy.ui.components.LoadingView
import com.mimokassy.ui.components.ErrorView
import com.mimokassy.ui.theme.CoolBlue
import com.mimokassy.ui.theme.RedBrown
import com.mimokassy.ui.theme.VertSauge
import com.mimokassy.ui.theme.Cassis
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotListScreen(
    navController: NavController
) {
    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ✅ Функция загрузки
    fun loadSlots() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitInstance.slotApi.listSlots()
                slots = response.items.map { slotResponse ->
                    Slot(
                        id = slotResponse.id,
                        startAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(slotResponse.start_at) ?: Date(),
                        program = Program(
                            id = slotResponse.program.id,
                            name = slotResponse.program.name,
                            description = slotResponse.program.description,
                            type = if (slotResponse.program.type == "simple") ProgramType.SIMPLE else ProgramType.COMPLEX,
                            durationMin = slotResponse.program.duration_min,
                            menuDescription = slotResponse.program.menu_description
                        ),
                        chef = Chef(
                            id = slotResponse.chef.id,
                            name = slotResponse.chef.name
                        ),
                        totalSeats = slotResponse.total_seats,
                        freeSeats = slotResponse.free_seats,
                        freeRentalKits = slotResponse.free_rental_kits,
                        price = slotResponse.price,
                        rentalPrice = slotResponse.rental_price,
                        studioName = slotResponse.studio_name,
                        studioAddress = slotResponse.studio_address,
                        studioLat = slotResponse.studio_lat,
                        studioLng = slotResponse.studio_lng,
                        status = if (slotResponse.status == "scheduled") SlotStatus.SCHEDULED else SlotStatus.CANCELLED
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
                isLoading = false
            }
        }
    }

    // ✅ Загружаем при первом открытии
    LaunchedEffect(Unit) {
        loadSlots()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Классы") },
                actions = {
                    IconButton(onClick = { showFiltersDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Фильтры",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingView()
                error != null -> ErrorView(
                    message = error!!,
                    onRetry = { loadSlots() }   // ✅ ПРОСТО ВЫЗОВ ФУНКЦИИ
                )
                slots.isEmpty() -> EmptyStateView(
                    title = "Пока нет доступных классов",
                    message = "Загляните позже — расписание обновляется.",
                    actionTitle = "Обновить",
                    onAction = { loadSlots() }
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(slots) { slot ->
                            SlotCardItem(
                                slot = slot,
                                onClick = {
                                    navController.navigate("slot/${slot.id}")
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    if (showFiltersDialog) {
        AlertDialog(
            onDismissRequest = { showFiltersDialog = false },
            title = { Text("Фильтры") },
            text = {
                Column {
                    Text("Фильтрация будет доступна после подключения бэкенда")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Параметры фильтрации:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text("• Дата/период")
                    Text("• Тип программы (простая/сложная)")
                    Text("• Шеф")
                    Text("• Только со свободными местами")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFiltersDialog = false },
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
fun SlotCardItem(
    slot: Slot,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = slot.displayDateTime,
                    style = MaterialTheme.typography.titleMedium
                )

                if (slot.isFull) {
                    Surface(
                        color = RedBrown,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Мест нет",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "${slot.freeSeats} из ${slot.totalSeats}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6C6C6C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = slot.program.name,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (slot.program.type == ProgramType.SIMPLE) VertSauge else Cassis,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = slot.program.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (slot.program.type == ProgramType.SIMPLE) Color(0xFF1A1A1A) else Color.White
                    )
                }

                Text(
                    text = "· ${slot.chef.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6C6C6C)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${slot.price} ₽",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

