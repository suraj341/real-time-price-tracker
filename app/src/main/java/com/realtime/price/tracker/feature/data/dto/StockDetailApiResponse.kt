package com.realtime.price.tracker.feature.data.dto

data class StockDetailApiResponse(
    val status: String,
    val stocks: List<StockDetailModel>
)

data class StockDetailModel(
    val symbol: String,
    val name: String,
    val price: Double,
    val currency: String,
    val description: String
)

