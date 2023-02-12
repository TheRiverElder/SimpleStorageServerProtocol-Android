package top.riverelder.android.riverlocalstorageserver

import android.app.Service
import android.content.Intent
import android.os.IBinder
import top.riverelder.android.riverlocalstorageserver.server.AndroidSimpleStorageServer
import java.lang.Exception

const val DEFAULT_PORT = 8888

class SimpleStorageServerService : Service() {

    var server: AndroidSimpleStorageServer? = null; private set

    override fun onBind(intent: Intent?): IBinder {
        return SimpleStorageServerBinder(this)
    }

    private fun checkAndGetServer(): AndroidSimpleStorageServer =
        (server ?: throw Exception("No server initialized!"))

    public fun initializeServer(port: Int = DEFAULT_PORT) {
        server = AndroidSimpleStorageServer(port)
    }

    public fun startServer() = checkAndGetServer().start()

    public fun stopServer() = checkAndGetServer().stop()
}