package com.realtime.price.tracker.feature.data.dto

data class StockDetailResponseModel(
    val symbol: String,
    val name: String,
    val price: Double,
    val currency: String,
    val details: String
)

