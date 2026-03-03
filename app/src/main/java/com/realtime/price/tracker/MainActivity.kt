package com.realtime.price.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.realtime.price.tracker.feature.data.MockDataGenerator
import com.realtime.price.tracker.feature.data.StockPriceDetailsRepository
import com.realtime.price.tracker.feature.data.StockPriceDetailsWebSocketDataSource
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import com.realtime.price.tracker.feature.presentation.StockPriceDetailScreen
import com.realtime.price.tracker.feature.presentation.StockPriceDetailScreenViewModel
import com.realtime.price.tracker.feature.presentation.StockPriceTrackerListScreenViewModel
import com.realtime.price.tracker.feature.presentation.StockPriceTrackerScreen
import kotlinx.serialization.Serializable

@Serializable
object StockListRoute

@Serializable
data class StockDetailRoute(val symbol: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initialize dependencies
        val mockDataGenerator = MockDataGenerator(applicationContext)
        val dataSource = StockPriceDetailsWebSocketDataSource(
            tokenProvider = { "your_token" },
            mockDataGenerator = mockDataGenerator
        )
        val repository = StockPriceDetailsRepository(dataSource)
        val useCase = StockPriceDetailsUseCase(repository)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = StockListRoute
            ) {
                composable<StockListRoute> {
                    val viewModel = remember {
                        StockPriceTrackerListScreenViewModel(useCase)
                    }

                    StockPriceTrackerScreen(
                        viewModel = viewModel,
                        onStockClick = { symbol ->
                            navController.navigate(StockDetailRoute(symbol))
                        }
                    )
                }

                composable<StockDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<StockDetailRoute>()
                    val viewModel = remember(route.symbol) {
                        StockPriceDetailScreenViewModel(
                            symbol = route.symbol,
                            stockPriceDetailsUseCase = useCase
                        )
                    }

                    StockPriceDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
