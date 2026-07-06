package com.mimokassy.ui.slots

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.models.*
import com.mimokassy.ui.components.LoadingView
import com.mimokassy.ui.components.ErrorView
import com.mimokassy.ui.theme.Cassis
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotCardScreen(
    navController: NavController,
    slotId: String
) {
    var slot by remember { mutableStateOf<Slot?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showMap by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadSlot() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitInstance.slotApi.getSlot(slotId)
                slot = Slot(
                    id = response.id,
                    startAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(response.start_at) ?: Date(),
                    program = Program(
                        id = response.program.id,
                        name = response.program.name,
                        description = response.program.description,
                        type = if (response.program.type == "simple") ProgramType.SIMPLE else ProgramType.COMPLEX,
                        durationMin = response.program.duration_min,
                        menuDescription = response.program.menu_description
                    ),
                    chef = Chef(
                        id = response.chef.id,
                        name = response.chef.name
                    ),
                    totalSeats = response.total_seats,
                    freeSeats = response.free_seats,
                    freeRentalKits = response.free_rental_kits,
                    price = response.price,
                    rentalPrice = response.rental_price,
                    studioName = response.studio_name,
                    studioAddress = response.studio_address,
                    studioLat = response.studio_lat,
                    studioLng = response.studio_lng,
                    status = if (response.status == "scheduled") SlotStatus.SCHEDULED else SlotStatus.CANCELLED
                )
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
                isLoading = false
            }
        }
    }

    LaunchedEffect(slotId) {
        loadSlot()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(slot?.program?.name ?: "Класс") },
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
        when {
            isLoading -> LoadingView()
            error != null -> ErrorView(
                message = error!!,
                onRetry = { loadSlot() }
            )
            slot == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Класс не найден",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }
            else -> {
                val currentSlot = slot!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Статус
                    if (currentSlot.isCancelled) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "❌ Класс отменён",
                                modifier = Modifier.padding(12.dp),
                                color = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = "Когда",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = currentSlot.displayDateTime,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Программа",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = currentSlot.program.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (currentSlot.program.type == ProgramType.SIMPLE)
                                Color.Green.copy(alpha = 0.2f)
                            else
                                Color(0xFFFF6B00).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = currentSlot.program.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            text = "· ${currentSlot.program.durationMin} мин",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    currentSlot.program.menuDescription?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Адрес студии",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        onClick = { showMap = true }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Карта",
                                tint = Color(0xFFFF6B00),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = currentSlot.studioAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Text(
                                text = "Открыть карту ›",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFF6B00)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Шеф",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = currentSlot.chef.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Места",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Свободно ${currentSlot.freeSeats} из ${currentSlot.totalSeats}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (currentSlot.isFull) Color.Red else Color(0xFF1A1A1A)
                        )
                        if (currentSlot.isFull) {
                            Surface(
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Мест нет",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Прокатная экипировка",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Свободно ${currentSlot.freeRentalKits} комплектов",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Цена",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${currentSlot.price} ₽ за место",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFF6B00)
                    )
                    if (currentSlot.rentalPrice > 0) {
                        Text(
                            text = "Прокат экипировки: ${currentSlot.rentalPrice} ₽ за комплект",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Оплата на месте: наличные или перевод на карту.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            navController.navigate("booking-form/${currentSlot.id}")
                        },
                        enabled = !currentSlot.isFull && !currentSlot.isCancelled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!currentSlot.isFull && !currentSlot.isCancelled)
                                Color(0xFFFF6B00)
                            else
                                Color.Gray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = if (currentSlot.isFull) "Мест нет" else "Записаться",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
