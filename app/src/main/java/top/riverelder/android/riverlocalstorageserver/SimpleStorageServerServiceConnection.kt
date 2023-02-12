package top.riverelder.android.riverlocalstorageserver

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast

class SimpleStorageServerServiceConnection(
    public val activity: MainActivity,
) : ServiceConnection {

    var service: SimpleStorageServerService? = null; private set

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        if (binder is SimpleStorageServerBinder) {
            service = binder.service
            Toast.makeText(activity, "绑定到Service", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        Toast.makeText(activity, "解绑Service", Toast.LENGTH_SHORT).show()
    }
}