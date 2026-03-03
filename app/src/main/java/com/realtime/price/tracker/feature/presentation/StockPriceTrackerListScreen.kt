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
import androidx.compose.material3.MaterialTheme
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
import com.realtime.price.tracker.ui.theme.Success
import com.realtime.price.tracker.ui.theme.Error

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
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
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
    val connectionColor = if (isConnected) Success else Error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

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
        PriceChange.UP -> Success
        PriceChange.DOWN -> Error
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
                    color = MaterialTheme.colorScheme.primary,
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
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stockData.currency,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.2f", stockData.price),
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }
}