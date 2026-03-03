package com.realtime.price.tracker.feature.presentation

import kotlinx.serialization.Serializable


@Serializable
object StockListRoute

@Serializable
data class StockDetailRoute(val symbol: String)