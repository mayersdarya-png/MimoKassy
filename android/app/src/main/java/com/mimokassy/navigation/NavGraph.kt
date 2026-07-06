package com.mimokassy.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mimokassy.ui.auth.LoginScreen
import com.mimokassy.ui.auth.OTPScreen
import com.mimokassy.ui.bookings.BookingDetailScreen
import com.mimokassy.ui.bookings.BookingFormScreen
import com.mimokassy.ui.bookings.BookingListScreen
import com.mimokassy.ui.profile.ProfileScreen
import com.mimokassy.ui.slots.SlotCardScreen
import com.mimokassy.ui.slots.SlotListScreen
import com.mimokassy.ui.theme.CoolBlue

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable(
            route = "otp/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OTPScreen(navController = navController, phone = phone)
        }

        composable("main") {
            MainScreen(navController = navController)
        }

        composable(
            route = "slot/{slotId}",
            arguments = listOf(navArgument("slotId") { type = NavType.StringType })
        ) { backStackEntry ->
            val slotId = backStackEntry.arguments?.getString("slotId") ?: ""
            SlotCardScreen(navController = navController, slotId = slotId)
        }

        composable(
            route = "booking/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingDetailScreen(
                navController = navController,
                bookingId = bookingId,
            )
        }

        composable(
            route = "booking-form/{slotId}",
            arguments = listOf(navArgument("slotId") { type = NavType.StringType })
        ) { backStackEntry ->
            val slotId = backStackEntry.arguments?.getString("slotId") ?: ""
            BookingFormScreen(navController = navController, slotId = slotId)
        }
    }
}

// ============================================
// MainScreen — с ручной вёрсткой меню
// ============================================

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    // Данные для пунктов меню
    val items = listOf(
        Triple(Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, "Классы"),
        Triple(Icons.Filled.List, Icons.Outlined.List, "Записи"),
        Triple(Icons.Filled.Person, Icons.Outlined.Person, "Профиль")
    )

    Scaffold(
        bottomBar = {
            // 🔥 Плавающее меню — СДЕЛАНО РУКАМИ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CoolBlue)
                    .height(72.dp),
                contentAlignment = Alignment.Center  // ← центрируем всё содержимое
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically  // ← иконки строго по центру
                ) {
                    items.forEachIndexed { index, (selectedIcon, unselectedIcon, title) ->
                        val isSelected = selectedTab == index

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,  // ← центрируем внутри колонки
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTab = index }
                        ) {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                contentDescription = title,
                                modifier = Modifier.size(28.dp),
                                tint = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF6C6C6C)
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF6C6C6C),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> SlotListScreen(navController = navController)
                1 -> BookingListScreen(navController = navController)
                2 -> ProfileScreen(navController = navController)
            }
        }
    }
}
