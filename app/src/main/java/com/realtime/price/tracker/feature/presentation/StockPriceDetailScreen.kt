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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.realtime.price.tracker.R
import com.realtime.price.tracker.ui.theme.Dimens
import com.realtime.price.tracker.ui.theme.Error
import com.realtime.price.tracker.ui.theme.Success

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
                            contentDescription = stringResource(R.string.navigate_back_content_description)
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
                    .padding(Dimens.paddingMedium)
            ) {
                ConnectionStatusIndicator(isConnected = state.value.isConnected)

                Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                Text(
                    text = state.value.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                PriceSection(
                    price = state.value.price,
                    currency = state.value.currency,
                    priceChange = state.value.priceChange
                )

                Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = Dimens.dividerThickness
                )

                Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                Text(
                    text = stringResource(R.string.about_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                Text(
                    text = state.value.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = Dimens.aboutSectionLineHeight
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(isConnected: Boolean) {
    val connectionColor = if (isConnected) Success else Error

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.connectionIndicatorSize)
                .clip(CircleShape)
                .background(connectionColor)
        )
        Text(
            text = if (isConnected) stringResource(R.string.connection_status_connected) else stringResource(R.string.connection_status_disconnected),
            modifier = Modifier.padding(start = Dimens.paddingSmall),
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
        PriceChange.UP -> Success
        PriceChange.DOWN -> Error
        PriceChange.NONE -> Color.Transparent
    }

    Column {
        Text(
            text = stringResource(R.string.current_price_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
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
                    fontSize = Dimens.priceIndicatorFontSize
                )
            }
        }

        Text(
            text = stringResource(R.string.currency_format, currency),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
