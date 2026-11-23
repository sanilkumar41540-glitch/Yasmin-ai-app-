package com.yasmin.ai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiClient(private val apiKey: String, private val model: String = "gemini-2.0-flash") {

    /** history: list of (role, text), role = "user" or "model". Blocking call; run off the main thread. */
    fun generate(system: String, history: List<Pair<String, String>>): String {
        if (apiKey.isBlank()) return "GEMINI_API_KEY set nahi hai. local.properties mein add karo."
        val body = JSONObject().apply {
            put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            put("contents", JSONArray().apply {
                history.forEach { (role, text) ->
                    put(JSONObject().put("role", role)
                        .put("parts", JSONArray().put(JSONObject().put("text", text))))
                }
            })
        }
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 60000
        }
        return try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().readText()
            if (conn.responseCode !in 200..299) return "Error ${conn.responseCode}: $text"
            JSONObject(text).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        } catch (e: Exception) {
            "Network error: ${e.message}"
        } finally {
            conn.disconnect()
        }
    }
}
