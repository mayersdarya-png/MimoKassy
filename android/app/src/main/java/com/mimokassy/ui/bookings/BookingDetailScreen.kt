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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.models.*
import com.mimokassy.ui.components.LoadingView
import com.mimokassy.ui.components.ErrorView
import com.mimokassy.ui.theme.Cassis
import com.mimokassy.ui.theme.RedBrown
import com.mimokassy.ui.theme.VertSauge
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    navController: NavController,
    bookingId: String
) {
    var booking by remember { mutableStateOf<Booking?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadBooking() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitInstance.bookingApi.getBooking(bookingId)
                booking = Booking(
                    id = response.id,
                    slotId = response.slot_id,
                    clientId = response.client_id,
                    seatsCount = response.seats_count,
                    rentalCount = response.rental_count,
                    status = when (response.status) {
                        "active" -> BookingStatus.ACTIVE
                        "cancelled" -> BookingStatus.CANCELLED
                        "late_cancel" -> BookingStatus.LATE_CANCEL
                        "studio_cancelled" -> BookingStatus.STUDIO_CANCELLED
                        else -> BookingStatus.ACTIVE
                    },
                    priceTotal = response.price_total,
                    allergyComment = response.allergy_comment,
                    createdAt = response.created_at,
                    cancelledAt = response.cancelled_at as String?,
                    cancellationReason = response.cancellation_reason,
                    slot = response.slot?.let { slotResponse ->
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
                )
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
                isLoading = false
            }
        }
    }

    LaunchedEffect(bookingId) {
        loadBooking()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Детали записи") },
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
                onRetry = { loadBooking() }
            )
            booking == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Запись не найдена",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }
            else -> {
                val currentBooking = booking!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (currentBooking.isPast) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                                Text(
                                    text = "Прошедшая",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = when (currentBooking.status) {
                                BookingStatus.ACTIVE -> VertSauge.copy(alpha = 0.2f)
                                else -> RedBrown.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (currentBooking.status) {
                                        BookingStatus.ACTIVE -> Icons.Filled.CheckCircle
                                        else -> Icons.Filled.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when (currentBooking.status) {
                                        BookingStatus.ACTIVE -> VertSauge
                                        else -> RedBrown
                                    }
                                )
                                Text(
                                    text = currentBooking.displayStatus,
                                    color = when (currentBooking.status) {
                                        BookingStatus.ACTIVE -> VertSauge
                                        else -> RedBrown
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Когда",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    currentBooking.slot?.let {
                        Text(
                            text = it.displayDateTime,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Программа",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    currentBooking.slot?.let { slot ->
                        Text(
                            text = slot.program.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = slot.program.displayName,
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
                    currentBooking.slot?.let { slot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
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
                                    text = slot.studioAddress,
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Шеф",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    currentBooking.slot?.let {
                        Text(
                            text = it.chef.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Места и экипировка",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Мест: ${currentBooking.seatsCount}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Своя: ${currentBooking.ownSeatsCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Прокат: ${currentBooking.rentalCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    currentBooking.allergyComment?.let {
                        Text(
                            text = "⚠️ Аллергии: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Цена",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${currentBooking.priceTotal} ₽",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFF6B00)
                    )

                    Text(
                        text = "Оплата на месте: наличные или перевод на карту.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Записано: ${currentBooking.createdAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    currentBooking.cancelledAt?.let {
                        Text(
                            text = "Отменено: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    currentBooking.cancellationReason?.let {
                        Text(
                            text = "Причина: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentBooking.canCancel) {
                        Button(
                            onClick = { showCancelDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cassis,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = if (isCancelling) "Отмена..." else "Отменить",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = "Отмена не позднее чем за 24 часа до старта — место освобождается. Позже — место остаётся за вами, но штрафов нет.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C6C6C),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (currentBooking.isPast) {
                        Text(
                            text = "Класс уже прошёл — отменить запись нельзя.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C6C6C),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (currentBooking.status != BookingStatus.ACTIVE) {
                        Text(
                            text = "Запись уже отменена.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C6C6C),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить запись?") },
            text = {
                Column {
                    Text("Отмена не позднее чем за 24 часа до старта — место освобождается. Позже — место остаётся за вами, но штрафов нет.")
                    booking?.slot?.let { slot ->
                        val hoursLeft = (slot.startAt.time - System.currentTimeMillis()) / (1000 * 60 * 60)
                        if (hoursLeft >= 24) {
                            Text(
                                text = "✅ Места вернутся в слот и станут доступны другим.",
                                color = VertSauge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                text = "⚠️ Поздняя отмена: место не освобождено (правило 24 часов). Штраф не взимается.",
                                color = Color(0xFFFF6B00),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isCancelling = true
                        scope.launch {
                            try {
                                val result = RetrofitInstance.bookingApi.cancelBooking(bookingId)
                                booking = Booking(
                                    id = result.id,
                                    slotId = result.slot_id,
                                    clientId = result.client_id,
                                    seatsCount = result.seats_count,
                                    rentalCount = result.rental_count,
                                    status = when (result.status) {
                                        "active" -> BookingStatus.ACTIVE
                                        "cancelled" -> BookingStatus.CANCELLED
                                        "late_cancel" -> BookingStatus.LATE_CANCEL
                                        "studio_cancelled" -> BookingStatus.STUDIO_CANCELLED
                                        else -> BookingStatus.ACTIVE
                                    },
                                    priceTotal = result.price_total,
                                    allergyComment = result.allergy_comment,
                                    createdAt = result.created_at,
                                    cancelledAt = result.cancelled_at as String?,
                                    cancellationReason = result.cancellation_reason,
                                    slot = booking?.slot
                                )
                                isCancelling = false
                                showCancelDialog = false
                            } catch (e: Exception) {
                                isCancelling = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RedBrown
                    )
                ) {
                    Text("Подтвердить отмену")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Не отменять")
                }
            }
        )
    }
}