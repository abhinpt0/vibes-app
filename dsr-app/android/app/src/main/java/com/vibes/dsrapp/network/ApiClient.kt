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

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
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
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        response.body?.string() ?: error("Empty response")
    }

    private fun post(json: String): Result<String> = runCatching {
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val response = client.newCall(Request.Builder().url(WEB_APP_URL).post(body).build()).execute()
        response.body?.string() ?: error("Empty response")
    }
}
