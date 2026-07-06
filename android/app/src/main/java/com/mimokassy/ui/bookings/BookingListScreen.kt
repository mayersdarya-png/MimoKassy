package com.mimokassy.ui.bookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.data.models.*
import com.mimokassy.ui.components.EmptyStateView
import com.mimokassy.ui.components.LoadingView
import com.mimokassy.ui.components.ErrorView
import com.mimokassy.ui.theme.VertSauge
import com.mimokassy.ui.theme.RedBrown
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    navController: NavController
) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadBookings() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitInstance.bookingApi.listBookings()
                bookings = response.items.map { bookingResponse ->
                    Booking(
                        id = bookingResponse.id,
                        slotId = bookingResponse.slot_id,
                        clientId = bookingResponse.client_id,
                        seatsCount = bookingResponse.seats_count,
                        rentalCount = bookingResponse.rental_count,
                        status = when (bookingResponse.status) {
                            "active" -> BookingStatus.ACTIVE
                            "cancelled" -> BookingStatus.CANCELLED
                            "late_cancel" -> BookingStatus.LATE_CANCEL
                            "studio_cancelled" -> BookingStatus.STUDIO_CANCELLED
                            else -> BookingStatus.ACTIVE
                        },
                        priceTotal = bookingResponse.price_total,
                        allergyComment = bookingResponse.allergy_comment,
                        createdAt = bookingResponse.created_at,
                        cancelledAt = bookingResponse.cancelled_at as String?,
                        cancellationReason = bookingResponse.cancellation_reason,
                        slot = bookingResponse.slot?.let { slotResponse ->
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
                }
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadBookings()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Мои записи") },
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
                    onRetry = { loadBookings() }
                )
                bookings.isEmpty() -> EmptyStateView(
                    title = "У вас пока нет записей",
                    message = "Запишитесь на кулинарный класс, чтобы начать.",
                    actionTitle = "Записаться",
                    onAction = { navController.navigate("slots") }
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val upcoming = bookings.filter { it.isUpcoming }
                        if (upcoming.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Предстоящие",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(upcoming) { booking ->
                                BookingCardItem(
                                    booking = booking,
                                    onClick = {
                                        navController.navigate("booking/${booking.id}")
                                    }
                                )
                            }
                        }

                        val past = bookings.filter { it.isPast }
                        if (past.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Прошедшие",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                            }
                            items(past) { booking ->
                                BookingCardItem(
                                    booking = booking,
                                    onClick = {
                                        navController.navigate("booking/${booking.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCardItem(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
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
                Surface(
                    color = when {
                        booking.isPast -> Color.Gray.copy(alpha = 0.1f)
                        booking.status == BookingStatus.ACTIVE -> VertSauge.copy(alpha = 0.2f)
                        else -> RedBrown.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                booking.isPast -> Icons.Filled.AccessTime
                                booking.status == BookingStatus.ACTIVE -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.Cancel
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = booking.displayStatus,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (booking.slot != null) {
                    Text(
                        text = booking.slot.displayDateTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            booking.slot?.let { slot ->
                Text(
                    text = slot.program.name,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = slot.chef.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${booking.seatsCount} мест · ${booking.rentalCount} прокатных",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Text(
                        text = "${booking.priceTotal} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B00)
                    )
                }

                booking.allergyComment?.let {
                    Text(
                        text = "⚠️ $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B00)
                    )
                }
            }
        }
    }
}
