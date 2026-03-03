package com.realtime.price.tracker.feature.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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

    if (state.value.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
        ) {
            stickyHeader {
                Text(
                    text = "Feed Screen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                )
            }
            items(
                items = state.value.stocks,
                key = { it.stock.symbol }
            ) { stockItem ->
                StockItem(stockItem)
            }
        }
    }
}

@Composable
fun StockItem(stockItem: StockItemUiModel) {
    val stockData = stockItem.stock
    val priceChange = stockItem.priceChange
    
    val indicatorText = when (priceChange) {
        PriceChange.UP -> "↑"
        PriceChange.DOWN -> "↓"
        PriceChange.NONE -> ""
    }
    val indicatorColor = when (priceChange) {
        PriceChange.UP -> Color(0xFF4CAF50)  // Green
        PriceChange.DOWN -> Color(0xFFE53935)  // Red
        PriceChange.NONE -> Color.Transparent
    }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stockData.symbol,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(9.dp)
                        .width(60.dp)
                )
                if (indicatorText.isNotEmpty()) {
                    Text(
                        text = indicatorText,
                        color = indicatorColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(text = String.format("$%.2f", stockData.price))
        }
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    }
}