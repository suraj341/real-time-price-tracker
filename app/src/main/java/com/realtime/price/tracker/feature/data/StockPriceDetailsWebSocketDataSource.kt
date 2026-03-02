package com.realtime.price.tracker.feature.data

import com.realtime.price.tracker.feature.data.dto.StockDetailResponseModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow

sealed class StockDetailsResult {
    data class Success(
        val stocks: List<StockDetailResponseModel>
    ) : StockDetailsResult()
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : StockDetailsResult()
}

class StockPriceDetailsWebSocketDataSource(
    private val tokenProvider: () -> String,
    private val retryConfig: RetryConfig = RetryConfig()
) {
    data class RetryConfig(
        val maxRetries: Int = 5,
        val initialDelayMillis: Long = 1000L,
        val maxDelayMillis: Long = 30000L,
        val backoffMultiplier: Double = 2.0
    )

    companion object {
        const val WEBSOCKET_URL = "wss://ws.postman-echo.com/raw"

        private const val STATUS_SUCCESS = "success"
        private const val STATUS_ERROR = "status"

        private const val CLOSE_NORMAL = 1000
        private const val CLOSE_AUTH_EXPIRED = 4001
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var webSocket: WebSocket? = null
    private var isConnected = false

    private val _stockUpdates = MutableSharedFlow<List<StockDetailResponseModel>>(
        replay = 1,
        extraBufferCapacity = 10
    )

    fun observeStockPriceDetails(): Flow<StockDetailsResult> = flow<StockDetailsResult> {
        ensureConnected()

        _stockUpdates.collect { stocks ->
            emit(StockDetailsResult.Success(stocks))
        }
    }.retryWhen { cause, attempt ->
        if (cause is CancellationException) {
            return@retryWhen false
        }

        if (isRetryableError(cause) && attempt < retryConfig.maxRetries) {
            val delayMillis = calculateBackoffDelay(attempt.toInt())
            delay(delayMillis)
            true
        } else {
            false
        }
    }.catch { cause: Throwable ->
        emit(StockDetailsResult.Error(cause))
        disconnect()
    }

    fun disconnect() {
        webSocket?.close(CLOSE_NORMAL, "Client disconnecting")
        webSocket = null
        isConnected = false
        scope.cancel()
    }

    private fun ensureConnected() {
        if (webSocket == null || !isConnected) {
            connect()
        }
    }

    private fun connect() {
        val request = Request.Builder()
            .url(WEBSOCKET_URL)
            .header("Authorization", "Bearer ${tokenProvider()}")
            .header("X-Client-Version", "1.0")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val status = json.optString("status", STATUS_SUCCESS)

                    if (status == STATUS_ERROR) {
                        handleError(json)
                        return
                    }

                    // Parse the stock list from the message
                    val stocks = parseStockList(json)
                    _stockUpdates.tryEmit(stocks)

                } catch (_: Exception) {
                    // Log error, don't crash the connection
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                this@StockPriceDetailsWebSocketDataSource.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                this@StockPriceDetailsWebSocketDataSource.webSocket = null
            }
        })
    }

    private fun handleError(json: JSONObject) {
        val errorCode = json.optString("code", "UNKNOWN_ERROR")

        when (errorCode) {
            "AUTH_EXPIRED" -> {
                webSocket?.close(CLOSE_AUTH_EXPIRED, "Token expired")
            }
            // Other errors are handled via Flow error handling
        }
    }

    private fun parseStockList(json: JSONObject): List<StockDetailResponseModel> {
        val stocksArray = json.optJSONArray("stocks") ?: return emptyList()

        return (0 until stocksArray.length()).map { index ->
            val stockJson = stocksArray.getJSONObject(index)
            StockDetailResponseModel(
                symbol = stockJson.getString("symbol"),
                name = stockJson.optString("name", ""),
                price = stockJson.optDouble("price", 0.0),
                currency = stockJson.optString("currency", "USD"),
                details = stockJson.optString("details", "")
            )
        }
    }

    private fun isRetryableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.net.SocketException -> true
            is java.net.SocketTimeoutException -> true
            is java.net.UnknownHostException -> true
            else -> false
        }
    }

    private fun calculateBackoffDelay(attempt: Int): Long {
        val exponentialDelay = (retryConfig.initialDelayMillis *
            retryConfig.backoffMultiplier.pow(attempt.toDouble())).toLong()
        return min(exponentialDelay, retryConfig.maxDelayMillis)
    }
}
