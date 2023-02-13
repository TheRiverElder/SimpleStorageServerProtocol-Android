package top.riverelder.android.riverlocalstorageserver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import top.riverelder.android.riverlocalstorageserver.server.AndroidSimpleStorageServer
import java.lang.Exception

const val DEFAULT_HTTP_PORT = 8888
const val DEFAULT_HTTPS_PORT = 8889

class SimpleStorageServerService : Service() {

    var httpServer: AndroidSimpleStorageServer? = null; private set
    var httpsServer: AndroidSimpleStorageServer? = null; private set

    override fun onBind(intent: Intent?): IBinder {
        return SimpleStorageServerBinder(this)
    }

    private fun checkAndGetServers(): List<AndroidSimpleStorageServer> {
        val result = listOf(httpServer, httpsServer);
        if (result.contains(null)) throw Exception("No server initialized!")
        return result.filterNotNull()
    }

    public fun initializeServers(httpPort: Int = DEFAULT_HTTPS_PORT, httpsPort: Int = DEFAULT_HTTPS_PORT, context: Context) {
        initializeHttpServer(httpPort)
        initializeHttpsServer(httpsPort, context)
    }


    public fun initializeHttpServer(port: Int = DEFAULT_HTTP_PORT) {
        val server = AndroidSimpleStorageServer(port)
        server.initialize(null)
        this.httpServer = server
    }

    public fun initializeHttpsServer(port: Int = DEFAULT_HTTPS_PORT, context: Context) {
        val server = AndroidSimpleStorageServer(port)
        server.initialize(context)
        this.httpsServer = server
    }

    public fun startServer() = checkAndGetServers().forEach { it.start() }

    public fun stopServer() = checkAndGetServers().forEach { it.stop() }
}