package top.riverelder.android.riverlocalstorageserver.server

import fi.iki.elonen.NanoHTTPD
import io.theriverelder.sssp.common.HttpResponseHelper
import io.theriverelder.sssp.common.model.JsonResponseBody

class AndroidSimpleStorageServer(port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession?): Response {
        if (session == null) return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR,
            "application/json",
            JsonResponseBody("Session is null").toJsonObject().toString()
        )
        return try {
            val supporter = HttpSessionResponseSupporter(session)
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