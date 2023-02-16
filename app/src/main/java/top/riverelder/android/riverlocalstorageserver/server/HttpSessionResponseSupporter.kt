package top.riverelder.android.riverlocalstorageserver.server

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.IStatus
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import io.theriverelder.sssp.common.ResponseSupporter
import java.io.InputStream
import java.lang.Long.parseLong
import java.net.URI
import kotlin.Exception

class HttpSessionResponseSupporter(
    private val session: NanoHTTPD.IHTTPSession,
    private val isHttps: Boolean,
) : ResponseSupporter {

    private var status: IStatus? = null
    private var contentLength: Long = -1L
    private val headers: MutableMap<String, String> = HashMap()
    private var responseBody: InputStream? = null

    override fun getRequestUri(): URI {
        val protocol = if (isHttps) "https" else "http"
        return URI("${protocol}://0.0.0.0${session.uri}?${session.queryParameterString}")
    }

    override fun getRequestBody(): InputStream = session.inputStream

    override fun getRequestBodyLength(): Long {
        return try {
            parseLong(getContentLength(session.headers) ?: return -1)
        } catch (ignored: Exception) {
            -1
        }
    }

    override fun setResponseHeader(name: String, value: String) {
        headers[name] = value
    }

    override fun setResponseStatus(statusCode: Int) {
        status = Status.values().find { status -> status.requestStatus == statusCode } ?: Status.OK
    }

    override fun setResponseBodyLength(contentLength: Long) {
        this.contentLength = contentLength
    }

    override fun sendResponseBody(responseBody: InputStream): Boolean {
        this.responseBody = responseBody
        return false
    }

    public fun generate(): NanoHTTPD.Response {
        if (status == null) throw Exception("Status code has not been set")
        if (contentLength < 0) throw Exception("Content length has not been set")
        if (responseBody == null) throw Exception("Response body has not been set")

        val contentType = headers["Content-Type"] ?: "text/plain"

        val response = newFixedLengthResponse(status, contentType, responseBody, contentLength)
        headers.forEach { response.addHeader(it.key, it.value) }
        return response
    }
}

fun getContentLength(headers: Map<String, String?>): String? {
    return headers["Content-Length"]
        ?: headers["content-Length"]
        ?: headers["Content-length"]
        ?: headers["content-length"]
}