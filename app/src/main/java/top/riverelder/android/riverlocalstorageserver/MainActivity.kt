package top.riverelder.android.riverlocalstorageserver

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Integer.parseInt
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


class MainActivity : AppCompatActivity() {

    //#region UI setup

    private lateinit var buttonInitialize: Button
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button
    private lateinit var editTextPortInput: EditText
    private lateinit var listViewUrlList: ListView

    private fun setupViews() {
        buttonInitialize = findViewById(R.id.buttonInitialize)
        buttonStart = findViewById(R.id.buttonStart)
        buttonStop = findViewById(R.id.buttonStop)
        editTextPortInput = findViewById(R.id.editTextPortInput)
        listViewUrlList = findViewById(R.id.listViewUrlList)

        editTextPortInput.inputType = InputType.TYPE_CLASS_NUMBER

        buttonInitialize.setOnClickListener { onClickButtonInitialize() }
        buttonStart.setOnClickListener { onClickButtonStart() }
        buttonStop.setOnClickListener { onClickButtonStop() }

        listViewUrlList.setOnItemClickListener {
                _, _, position, _ ->

                val url: String = listViewUrlList.adapter.getItem(position) as String
                val clipboardManager = (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?)
                clipboardManager?.setPrimaryClip(ClipData.newPlainText("url", url))
        }
    }

    //#endregion

    private val port: Int get() {
        return try { parseInt(editTextPortInput.text.toString()) } catch (e: Exception) { DEFAULT_PORT }
    }

    private val connection = SimpleStorageServerServiceConnection(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()

        //绑定Service
        val intent = Intent("top.riverelder.android.riverlocalstorageserver.SIMPLE_STORAGE_SERVER")
        intent.setPackage(packageName)
        intent.setClass(this, SimpleStorageServerService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        ensureReadWritePermission()
    }

    private fun ensureReadWritePermission() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
//            Environment.isExternalStorageManager()) {
//            Toast.makeText(this, "已从该死的新Android访问所有文件权限", Toast.LENGTH_SHORT).show()
//        } else {
//            getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//            val builder = AlertDialog.Builder(this)
//                .setMessage("快给本喵全部文件访问权限！")
//                .setPositiveButton("干TMD生儿子没PY的Google！") { _, _ ->
//                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//                    startActivity(intent)
//                }
//            builder.show()
//        }
    }

    private fun checkAndGetService() = connection.service ?: throw Exception("No service connected!")

    private fun onClickButtonInitialize() {
        val port = port
        if (toastExceptionMessage { checkAndGetService().initializeServer(port) }) {
            Toast.makeText(this, "初始化完成，端口：$port", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onClickButtonStart() {
        if (toastExceptionMessage { checkAndGetService().startServer() }) {
            refreshIpList()
            Toast.makeText(this, "已开启", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshIpList() {
        Thread {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf(getIPAddress()).map { "http://$it:$port/" })
            runOnUiThread {
                listViewUrlList.adapter = adapter
            }
        }.start()
    }

    private fun onClickButtonStop() {
        if (toastExceptionMessage { checkAndGetService().stopServer() }) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf<String>())
            listViewUrlList.adapter = adapter
            Toast.makeText(this, "已关闭", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toastExceptionMessage(fn: () -> Unit): Boolean {
        return try {
            fn()
            true
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            false
        }
    }


    private fun getIPAddress(): String? {
        val context = this

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val addresses = connectivityManager.activeNetwork?.getAllByName("localhost")
            Log.d("GET_IP_ADDRESS", Arrays.toString(addresses))
        }

        val info =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (info != null && info.isConnected) {
            if (info.type == ConnectivityManager.TYPE_MOBILE) { //当前使用2G/3G/4G网络
                try {
                    for (element in NetworkInterface.getNetworkInterfaces()) {
                        for (address in element.inetAddresses) {
                            if (address is Inet4Address) {
                                return address.getHostAddress()
                            }
                        }
                    }
                } catch (e: SocketException) {
                    return "<No network>"
                }
            } else if (info.type == ConnectivityManager.TYPE_WIFI) { //当前使用无线网络
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                return intIP2StringIP(wifiInfo.ipAddress)
            }
        } else {
            //当前无网络连接,请在设置中打开网络
            return "<No network>"
        }
        return "<No network>"
    }
}

fun intIP2StringIP(intValue: Int): String {
    val builder = StringBuilder()
    builder.append((intValue) and 0xFF).append(".")
    builder.append((intValue ushr 8) and 0xFF).append(".")
    builder.append((intValue ushr 16) and 0xFF).append(".")
    builder.append((intValue ushr 24) and 0xFF)
    return builder.toString()
}