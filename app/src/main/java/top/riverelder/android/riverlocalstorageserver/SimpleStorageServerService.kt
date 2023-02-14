package top.riverelder.android.riverlocalstorageserver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import top.riverelder.android.riverlocalstorageserver.server.AndroidSimpleStorageServer
import kotlin.Exception

const val DEFAULT_HTTP_PORT = 8888
const val DEFAULT_HTTPS_PORT = 8889

class SimpleStorageServerService : Service() {

    var httpServer: AndroidSimpleStorageServer? = null; private set
    var httpsServer: AndroidSimpleStorageServer? = null; private set

    override fun onBind(intent: Intent?): IBinder {
        return SimpleStorageServerBinder(this)
    }

    private fun checkAndGetServers(): List<AndroidSimpleStorageServer> {
        val result = listOf(httpServer, httpsServer)
        if (result.contains(null)) throw Exception("No server initialized!")
        return result.filterNotNull()
    }

    fun initializeServers(httpPort: Int = DEFAULT_HTTPS_PORT, httpsPort: Int = DEFAULT_HTTPS_PORT, context: Context) {
        initializeHttpServer(httpPort)
        initializeHttpsServer(httpsPort, context)
    }


    fun initializeHttpServer(port: Int = DEFAULT_HTTP_PORT) {
        val server = AndroidSimpleStorageServer(port)
        server.initialize(null)
        this.httpServer = server
    }

    fun initializeHttpsServer(port: Int = DEFAULT_HTTPS_PORT, context: Context) {
        val server = AndroidSimpleStorageServer(port)
        server.initialize(context)
        this.httpsServer = server
    }

    fun startServer() {
        checkAndGetServers().forEach { it.start() }
        try {
            lock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire(10 * 60 * 1000L /*10 minutes*/)
                }
            }
        } catch (ignored: Exception) { }
    }

    fun stopServer() {
        checkAndGetServers().forEach { it.stop() }
        try {
            val lock = this.lock
            if (lock != null && lock.isHeld) {
                lock.release()
            }
        } catch (ignored: Exception) { }
    }

    var lock: PowerManager.WakeLock? = null
}