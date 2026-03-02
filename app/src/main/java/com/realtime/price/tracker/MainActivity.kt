package com.realtime.price.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.realtime.price.tracker.feature.data.MockDataGenerator
import com.realtime.price.tracker.feature.data.StockPriceDetailsRepository
import com.realtime.price.tracker.feature.data.StockPriceDetailsWebSocketDataSource
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import com.realtime.price.tracker.feature.presentation.StockPriceTrackerListScreenViewModel
import com.realtime.price.tracker.feature.presentation.StockPriceTrackerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val mockDataGenerator = MockDataGenerator(applicationContext)
        val dataSource = StockPriceDetailsWebSocketDataSource(tokenProvider = { "your_token" }, mockDataGenerator = mockDataGenerator)
        val repository = StockPriceDetailsRepository(dataSource)
        val useCase = StockPriceDetailsUseCase(repository)
        val viewModel = StockPriceTrackerListScreenViewModel(useCase)

        setContent {
            StockPriceTrackerScreen(viewModel = viewModel)
        }
    }
}