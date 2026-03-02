package com.realtime.price.tracker.feature.data

import kotlinx.coroutines.flow.Flow

class StockPriceDetailsRepository(
    private val webSocketDataSource: StockPriceDetailsWebSocketDataSource
) {
    fun observeStockPriceDetails(stockSymbol: String): Flow<StockDetailsResult> {
        return webSocketDataSource.observeStockPriceDetails()
    }
}
