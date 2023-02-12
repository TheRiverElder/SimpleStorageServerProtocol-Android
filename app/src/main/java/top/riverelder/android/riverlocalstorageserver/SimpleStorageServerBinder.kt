package top.riverelder.android.riverlocalstorageserver

import android.os.Binder

class SimpleStorageServerBinder(
    public val service: SimpleStorageServerService
) : Binder()