package com.mimokassy.ui.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.api.CreateBookingRequest
import com.mimokassy.data.models.*
import com.mimokassy.ui.components.LoadingView
import com.mimokassy.ui.components.ErrorView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    navController: NavController,
    slotId: String
) {
    var slot by remember { mutableStateOf<Slot?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var seatsCount by remember { mutableStateOf(1) }
    var rentalStatuses by remember { mutableStateOf(listOf(false)) }
    var allergyComment by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    var isBooking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val rentalCount = rentalStatuses.count { it }
    val totalPrice = slot?.let {
        it.price * seatsCount + it.rentalPrice * rentalCount
    } ?: 0

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
                title = { Text("Оформление записи") },
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
                val maxSeats = minOf(currentSlot.freeSeats, currentSlot.program.capacityCap, 3)
                val canBook = seatsCount > 0 && seatsCount <= maxSeats && rentalCount <= currentSlot.freeRentalKits

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = currentSlot.displayDateTime,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${currentSlot.program.name} · ${currentSlot.chef.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Свободно мест: ${currentSlot.freeSeats}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "·",
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Прокат: ${currentSlot.freeRentalKits} комплектов",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Число мест",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (seatsCount > 1) {
                                    seatsCount--
                                    if (rentalStatuses.size > seatsCount) {
                                        rentalStatuses = rentalStatuses.take(seatsCount)
                                    }
                                }
                            },
                            enabled = seatsCount > 1
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircle,
                                contentDescription = "Уменьшить",
                                tint = if (seatsCount > 1) Color(0xFFFF6B00) else Color.Gray
                            )
                        }

                        Text(
                            text = seatsCount.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(48.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                if (seatsCount < maxSeats) {
                                    seatsCount++
                                    rentalStatuses = rentalStatuses + false
                                }
                            },
                            enabled = seatsCount < maxSeats
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "Увеличить",
                                tint = if (seatsCount < maxSeats) Color(0xFFFF6B00) else Color.Gray
                            )
                        }

                        Text(
                            text = "Макс. $maxSeats",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "Можно записать до $maxSeats мест",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Экипировка для каждого места",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    for (i in 0 until seatsCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (i == 0) "Место ${i + 1} (вы)" else "Место ${i + 1} (гость)",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !rentalStatuses[i],
                                    onClick = {
                                        val newList = rentalStatuses.toMutableList()
                                        newList[i] = false
                                        rentalStatuses = newList
                                    },
                                    label = { Text("Своя") },
                                    enabled = true,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF6B00).copy(alpha = 0.1f),
                                        selectedLabelColor = Color(0xFFFF6B00)
                                    )
                                )
                                FilterChip(
                                    selected = rentalStatuses[i],
                                    onClick = {
                                        if (rentalCount < currentSlot.freeRentalKits || rentalStatuses[i]) {
                                            val newList = rentalStatuses.toMutableList()
                                            newList[i] = true
                                            rentalStatuses = newList
                                        }
                                    },
                                    label = { Text("Прокатная") },
                                    enabled = rentalCount < currentSlot.freeRentalKits || rentalStatuses[i],
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF6B00).copy(alpha = 0.1f),
                                        selectedLabelColor = Color(0xFFFF6B00)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = "Прокатных выбрано: $rentalCount из ${currentSlot.freeRentalKits}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Аллергии и особенности питания",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = allergyComment,
                        onValueChange = { allergyComment = it },
                        placeholder = { Text("Напишите, если есть аллергии или особые пожелания") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B00)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Цена",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Column {
                        Text(
                            text = "Места: ${currentSlot.price} ₽ × $seatsCount",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (rentalCount > 0) {
                            Text(
                                text = "Прокат: ${currentSlot.rentalPrice} ₽ × $rentalCount",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalPrice ₽",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Text(
                        text = "Оплата на месте: наличные или перевод на карту.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isBooking = true
                            scope.launch {
                                try {
                                    val idempotencyKey = UUID.randomUUID().toString()
                                    val request = CreateBookingRequest(
                                        slot_id = slotId,
                                        seats_count = seatsCount,
                                        rental_count = rentalCount,
                                        allergy_comment = allergyComment
                                    )
                                    val response = RetrofitInstance.bookingApi.createBooking(
                                        idempotencyKey = idempotencyKey,
                                        request = request
                                    )
                                    if (response.id.isNotEmpty()) {
                                        showSuccess = true
                                    }
                                    isBooking = false
                                } catch (e: Exception) {
                                    isBooking = false
                                    error = e.message ?: "Ошибка создания брони"
                                }
                            }
                        },
                        enabled = canBook && !isBooking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canBook && !isBooking) Color(0xFFFF6B00) else Color.Gray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = if (isBooking) "Оформление..." else "Записаться · $totalPrice ₽",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (showSuccess && slot != null) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Успех",
                    tint = Color.Green,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Вы записаны!") },
            text = {
                Column {
                    Text(slot!!.program.name)
                    Text(slot!!.displayDateTime)
                    Text("$seatsCount мест · $rentalCount прокатных")
                    Text("Итого: $totalPrice ₽")
                    Text(
                        text = "Оплата на месте: наличные или перевод на карту.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccess = false
                        navController.navigate("main") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                ) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccess = false
                        navController.navigate("bookings")
                    }
                ) {
                    Text("Мои бронирования")
                }
            }
        )
    }
}
