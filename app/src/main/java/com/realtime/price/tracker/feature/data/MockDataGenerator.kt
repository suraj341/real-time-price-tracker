package com.realtime.price.tracker.feature.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import com.realtime.price.tracker.feature.data.dto.StockDetailResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

class MockDataGenerator(private val context: Context) {

    private var stockList: MutableList<StockDetailModel>? = null
    private val gson = Gson()
    private val UPDATE_INTERVAL_MS = 2000L
    private val PRICE_CHANGE_PERCENTAGE = 0.02 // 2% max change

    fun generateMockStockPrices(): Flow<String> = flow {
        // Initialize the list once from JSON
        if (stockList == null) {
            stockList = loadStockDataFromJson(context).toMutableList()
        }

        while (true) {
            // Create response model with success status
            val responseModel = StockDetailResponseModel(
                status = "success",
                stocks = stockList!!.toList()
            )

            // Convert to JSON string and emit
            emit(gson.toJson(responseModel))

            // Wait 2 seconds before next update
            delay(UPDATE_INTERVAL_MS)

            // Modify prices in place to reuse the list
            updatePricesRandomly()
        }
    }.flowOn(Dispatchers.IO)

    private fun loadStockDataFromJson(context: Context): List<StockDetailModel> {
        val jsonString = context.assets.open("Sample.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<StockDetailModel>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    private fun updatePricesRandomly() {
        stockList?.let { list ->
            for (i in list.indices) {
                val currentStock = list[i]
                val changeFactor =
                    1 + (Random.nextDouble(-PRICE_CHANGE_PERCENTAGE, PRICE_CHANGE_PERCENTAGE))
                val newPrice = currentStock.price * changeFactor

                // Update in place to avoid creating new objects
                list[i] = currentStock.copy(price = newPrice)
            }
        }
    }
}
