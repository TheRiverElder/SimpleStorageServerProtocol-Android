package top.riverelder.android.riverlocalstorageserver.server

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.IStatus
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import io.theriverelder.sssp.common.ResponseSupporter
import java.io.InputStream
import java.lang.Exception
import java.net.URI

class HttpSessionResponseSupporter(
    private val session: NanoHTTPD.IHTTPSession,
) : ResponseSupporter {

    private var status: IStatus? = null
    private var contentLength: Long = -1L
    private val headers: MutableMap<String, String> = HashMap()
    private var responseBody: InputStream? = null

    override fun getRequestUri(): URI =
        URI("http", null, session.uri, session.queryParameterString, null)

    override fun getRequestBody(): InputStream = session.inputStream

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

        return newFixedLengthResponse(status, contentType, responseBody, contentLength)
    }
}