package com.realtime.price.tracker.feature.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class StockPriceDetailsRepository(
    private val webSocketDataSource: StockPriceDetailsWebSocketDataSource
) {
    val isConnected: StateFlow<Boolean> = webSocketDataSource.isConnected

    fun observeStockPriceDetails(): Flow<StockDetailsResult> {
        return webSocketDataSource.observeStockPriceDetails()
    }

    fun disconnect() {
        webSocketDataSource.disconnect()
    }
}
