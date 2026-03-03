package com.realtime.price.tracker.feature.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.realtime.price.tracker.ui.theme.RealTimePriceTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            RealTimePriceTrackerTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = StockListRoute
                ) {
                    composable<StockListRoute> {
                        StockPriceTrackerScreen(
                            viewModel = hiltViewModel(),
                            onStockClick = { symbol ->
                                navController.navigate(StockDetailRoute(symbol))
                            }
                        )
                    }

                    composable<StockDetailRoute> { backStackEntry ->
                        StockPriceDetailScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
