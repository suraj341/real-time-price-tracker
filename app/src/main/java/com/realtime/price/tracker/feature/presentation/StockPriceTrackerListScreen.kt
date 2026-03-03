package com.realtime.price.tracker.feature.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StockPriceTrackerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockPriceTrackerListScreenViewModel,
    onStockClick: (String) -> Unit = {}
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
                Header(
                    isConnected = state.value.isConnected,
                    onToggleClick = { viewModel.toggleConnection() }
                )
            }
            items(
                items = state.value.stocks,
                key = { it.stock.symbol }
            ) { stockItem ->
                StockItem(
                    stockItem = stockItem,
                    onClick = { onStockClick(stockItem.stock.symbol) }
                )
            }
        }
    }
}

@Composable
fun Header(
    isConnected: Boolean,
    onToggleClick: () -> Unit
) {
    val connectionColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Connection status indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(connectionColor)
            )
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Medium
            )
        }

        // Right: Toggle button
        Switch(
            checked = isConnected,
            onCheckedChange = { onToggleClick() }
        )
    }
}

@Composable
fun StockItem(
    stockItem: StockItemUiModel,
    onClick: () -> Unit
) {
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

    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
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
            // Right side: Currency and Price with fixed widths
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency (left-aligned in its box)
                Text(
                    text = stockData.currency,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Start
                )
                // Space between currency and price
                Spacer(modifier = Modifier.width(8.dp))
                // Price (right-aligned in its box)
                Text(
                    text = String.format("%.2f", stockData.price),
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End
                )
            }
        }
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    }
}