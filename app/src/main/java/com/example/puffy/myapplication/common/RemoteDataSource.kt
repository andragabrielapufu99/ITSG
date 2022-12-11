package com.example.puffy.myapplication.common

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

object RemoteDataSource {
    val eventChannel = Channel<String>()

    init {
        val request = Request.Builder().url("ws://${Api.baseURL}").build()
        val webSocket = OkHttpClient().newWebSocket(request, MyWebSocketListener())
    }

    private class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket","onOpen")
            val jsonToken = JSONObject()
            jsonToken.put("token",Api.tokenInterceptor.token)
            val jsonObj = JSONObject()
            jsonObj.put("type","authorization")
            jsonObj.put("payload",jsonToken)
            webSocket.send(jsonObj.toString())
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket","onMessage $text")
            runBlocking { eventChannel.send(text) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket","onMessage bytes")
            println("Receive $bytes bytes")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket","onFailure",t)
            t.printStackTrace()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket","onClosing")
        }
    }
}
