package com.example.puffy.myapplication.common

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData

class ConnectivityLiveData internal constructor(private val connectivityManager : ConnectivityManager) : LiveData<Boolean>() {

    constructor(application : Application) : this(application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    private val tagName: String = "ConnectivityLiveData"

    private val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP) object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            postValue(true)
            Log.i(tagName, "Online")
        }
        override fun onLost(network: Network) {
            postValue(false)
            Log.i(tagName, "Offline")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActive() {
        super.onActive()
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo //conectat sau neconectat
        postValue(activeNetwork?.isConnectedOrConnecting == true)
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        Log.i(tagName, "Register network callback")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        Log.i(tagName, "Unregister network callback")
    }

}