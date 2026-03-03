package com.realtime.price.tracker.feature.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPriceDetailScreen(
    viewModel: StockPriceDetailScreenViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.value.symbol) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(16.dp)
            ) {
                // Connection status indicator
                ConnectionStatusIndicator(isConnected = state.value.isConnected)

                Spacer(modifier = Modifier.height(24.dp))

                // Stock Name
                Text(
                    text = state.value.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price section with indicator
                PriceSection(
                    price = state.value.price,
                    currency = state.value.currency,
                    priceChange = state.value.priceChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                Spacer(modifier = Modifier.height(24.dp))

                // Description section
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.value.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(isConnected: Boolean) {
    val connectionColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935)

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
            color = connectionColor
        )
    }
}

@Composable
private fun PriceSection(
    price: Double,
    currency: String,
    priceChange: PriceChange
) {
    val indicatorText = when (priceChange) {
        PriceChange.UP -> "↑"
        PriceChange.DOWN -> "↓"
        PriceChange.NONE -> ""
    }
    val indicatorColor = when (priceChange) {
        PriceChange.UP -> Color(0xFF4CAF50)
        PriceChange.DOWN -> Color(0xFFE53935)
        PriceChange.NONE -> Color.Transparent
    }

    Column {
        Text(
            text = "Current Price",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format("%.2f", price),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            if (indicatorText.isNotEmpty()) {
                Text(
                    text = indicatorText,
                    color = indicatorColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            }
        }

        Text(
            text = "Currency: $currency",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
