package top.riverelder.android.riverlocalstorageserver

import android.net.ConnectivityManager
import android.net.Network

class MyNetworkCallback : ConnectivityManager.NetworkCallback() {

    var network: Network? = null; private set

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        this.network = network

        network.describeContents()
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        this.network = null
    }

//    fun getIpAddressList(): List<String> {
//        val network = this.network ?: return listOf()
//    }
}