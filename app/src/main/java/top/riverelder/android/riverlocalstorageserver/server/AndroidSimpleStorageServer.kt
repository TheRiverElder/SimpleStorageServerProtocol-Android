package top.riverelder.android.riverlocalstorageserver.server

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import io.theriverelder.sssp.common.HttpResponseHelper
import io.theriverelder.sssp.common.model.JsonResponseBody
import top.riverelder.android.riverlocalstorageserver.R
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory

class AndroidSimpleStorageServer(port: Int) : NanoHTTPD(port) {

    private var isHttps: Boolean = false

    // 如果context为null，则使用http，否则将尝试使用https
    fun initialize(context: Context?) {
        if (context == null) return
        isHttps = true
        val keystoreStream: InputStream = context.resources.openRawResource(R.raw.keystore)
        // 拿到keystore对象
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        // keystore加载流对象，并把storepass参数传入
        keyStore.load(keystoreStream, "password".toCharArray())
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        // 这里的第二个参数就是密钥密码，keypass
        keyManagerFactory.init(keyStore, "password".toCharArray())
        // 调用 NanoHttpd的makeSecure()方法
        makeSecure(makeSSLSocketFactory(keyStore, keyManagerFactory), null)
    }

    override fun serve(session: IHTTPSession?): Response {
        if (session == null) return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR,
            "application/json",
            JsonResponseBody("Session is null").toJsonObject().toString()
        )
        return try {
            val supporter = HttpSessionResponseSupporter(session, isHttps)
            HttpResponseHelper.process(supporter)
            supporter.generate()
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                JsonResponseBody(e.message ?: "").toJsonObject().toString()
            )
        }
    }


}