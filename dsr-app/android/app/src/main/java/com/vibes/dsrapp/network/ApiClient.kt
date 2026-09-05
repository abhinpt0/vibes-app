package com.vibes.dsrapp.network

import com.google.gson.Gson
import com.vibes.dsrapp.model.SubmitPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val WEB_APP_URL =
        "https://script.google.com/macros/s/AKfycbwFC1oHoC3VWzGpY9ZlTFk6jpfy5gvHHhrOv2zd_MGQOxIJr2qcGrrngdbD1N3fRHPq/exec"

    // For GET requests: follow all redirects normally
    private val getClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    // For POST requests: do NOT auto-follow redirects.
    // Apps Script returns a 302 → Location URL. We re-POST to that URL manually
    // so the body is preserved. OkHttp's default redirect changes POST→GET (RFC).
    private val postClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private val gson = Gson()

    // ── GET ──────────────────────────────────────────────────────────────────

    suspend fun getDSRList(limit: Int = 30): Result<String> = withContext(Dispatchers.IO) {
        get("$WEB_APP_URL?action=getDSRList&limit=$limit")
    }

    suspend fun getDSRByDate(date: String): Result<String> = withContext(Dispatchers.IO) {
        get("$WEB_APP_URL?action=getDSR&date=$date")
    }

    suspend fun getRetailers(): Result<String> = withContext(Dispatchers.IO) {
        get("$WEB_APP_URL?action=getRetailers")
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    suspend fun submitDSR(payload: Any): Result<String> = withContext(Dispatchers.IO) {
        post(gson.toJson(payload))
    }

    suspend fun submitAll(payload: SubmitPayload): Result<String> = withContext(Dispatchers.IO) {
        post(gson.toJson(payload))
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun get(url: String): Result<String> = runCatching {
        val response = getClient.newCall(Request.Builder().url(url).get().build()).execute()
        response.body?.string() ?: error("Empty response")
    }

    /**
     * POST with manual redirect following.
     *
     * Google Apps Script web apps redirect POST requests with a 302.
     * OkHttp (per HTTP spec) turns the POST into a GET on redirect, so
     * Apps Script's doPost() never receives the body.
     *
     * We disable auto-redirect on postClient and manually re-POST to the
     * Location header URL up to 5 times.
     */
    private fun post(json: String): Result<String> = runCatching {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        var url = WEB_APP_URL
        var attempts = 0

        while (attempts < 5) {
            attempts++
            val body = json.toRequestBody(mediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = postClient.newCall(request).execute()

            when (response.code) {
                200, 201 -> {
                    return@runCatching response.body?.string() ?: error("Empty response")
                }
                301, 302, 303, 307, 308 -> {
                    val location = response.header("Location")
                        ?: error("Redirect with no Location header")
                    response.close()
                    url = location
                    // continue loop and re-POST to the new URL
                }
                else -> {
                    val body = response.body?.string() ?: ""
                    error("HTTP ${response.code}: $body")
                }
            }
        }
        error("Too many redirects")
    }
}
