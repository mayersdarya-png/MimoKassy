package com.mimokassy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mimokassy.data.api.RetrofitInstance
import com.mimokassy.navigation.NavGraph
import com.mimokassy.ui.theme.MimoKassyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ ИНИЦИАЛИЗИРУЕМ RetrofitInstance с контекстом
        RetrofitInstance.init(applicationContext)

        setContent {
            MimoKassyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}
