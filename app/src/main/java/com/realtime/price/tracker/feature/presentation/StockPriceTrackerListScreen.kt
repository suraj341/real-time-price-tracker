package com.realtime.price.tracker.feature.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.realtime.price.tracker.feature.data.dto.StockDetailModel

@Composable
fun StockPriceTrackerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockPriceTrackerListScreenViewModel
) {
    val state = viewModel.state.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        stickyHeader {
            Text(
                text = "Ticker",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            )
        }
        items(
            items = state.value,
            key = { it.symbol }      // MOST IMPORTANT for diffing
        ) { stock ->
            StockItem(stock)
        }
    }
}

@Composable
fun StockItem(stockData: StockDetailModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = stockData.name,
            color = Color.Blue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(9.dp)
        )
        Text(text = stockData.price.toString())
    }
}