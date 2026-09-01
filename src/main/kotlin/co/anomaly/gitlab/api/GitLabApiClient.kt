package co.anomaly.gitlab.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.util.concurrent.TimeUnit

class GitLabApiClient(
    private val baseUrl: String,
    private val privateToken: String?
) {

    companion object {
        private const val DEFAULT_API_VERSION = "4"
        private const val CONNECTION_TIMEOUT = 30L
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 30L
    }

    private val objectMapper: ObjectMapper = JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson: MediaType = "application/json".toMediaType()

    private val baseUrlNormalized: String = baseUrl.removeSuffix("/")

    val apiEndpoint: String
        get() = "$baseUrlNormalized/api/v$DEFAULT_API_VERSION"

    fun isConfigured(): Boolean {
        return baseUrl.isNotBlank()
    }

    fun isAuthenticated(): Boolean {
        return privateToken != null && privateToken.isNotBlank()
    }

    fun <T : Any?> get(path: String, responseType: Class<T>): T {
        val url = "$apiEndpoint$path"
        val request = Request.Builder()
            .url(url)
            .header("PRIVATE-TOKEN", privateToken.orEmpty())
            .get()
            .build()

        val response = client.newCall(request).execute()
        return processResponse(response, responseType)
    }

    fun <T : Any?> get(path: String, params: Map<String, String>, responseType: Class<T>): T {
        val httpUrl = "$apiEndpoint$path".toHttpUrl()
        val request = Request.Builder()
            .url(httpUrl.newBuilder().apply {
                params.forEach { (key, value) ->
                    if (value.isNotEmpty()) {
                        addQueryParameter(key, value)
                    }
                }
            }.build())
            .header("PRIVATE-TOKEN", privateToken.orEmpty())
            .get()
            .build()

        val response = client.newCall(request).execute()
        return processResponse(response, responseType)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getPage(path: String, page: Int, perPage: Int, itemType: Class<T>): List<T> {
        val params = mapOf(
            "page" to page.toString(),
            "per_page" to perPage.toString()
        )
        val result = get(path, params, List::class.java)
        return result as List<T>
    }

    fun <T : Any?> post(path: String, body: Any, responseType: Class<T>): T {
        val json = objectMapper.writeValueAsString(body)
        val requestBody = json.toRequestBody(mediaTypeJson)

        val url = "$apiEndpoint$path"
        val request = Request.Builder()
            .url(url)
            .header("PRIVATE-TOKEN", privateToken.orEmpty())
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        return processResponse(response, responseType)
    }

    fun <T : Any?> put(path: String, body: Any, responseType: Class<T>): T {
        val json = objectMapper.writeValueAsString(body)
        val requestBody = json.toRequestBody(mediaTypeJson)

        val url = "$apiEndpoint$path"
        val request = Request.Builder()
            .url(url)
            .header("PRIVATE-TOKEN", privateToken.orEmpty())
            .put(requestBody)
            .build()

        val response = client.newCall(request).execute()
        return processResponse(response, responseType)
    }

    fun <T : Any?> delete(path: String) {
        val url = "$apiEndpoint$path"
        val request = Request.Builder()
            .url(url)
            .header("PRIVATE-TOKEN", privateToken.orEmpty())
            .delete()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("DELETE request failed: ${response.code}")
        }
    }

    @Throws(IOException::class)
    private fun <T : Any?> processResponse(response: Response, responseType: Class<T>): T {
        return when {
            response.isSuccessful -> {
                val body = response.body?.string() ?: ""
                if (responseType == List::class.java) {
                    val listType: com.fasterxml.jackson.core.type.TypeReference<List<Any>> = object : com.fasterxml.jackson.core.type.TypeReference<List<Any>>() {}
                    objectMapper.readValue(body, listType) as T
                } else {
                    objectMapper.readValue(body, responseType)
                }
            }
            response.code == 401 -> throw SecurityException("Authentication failed. Check your GitLab token.")
            response.code == 403 -> throw SecurityException("Access forbidden. Check your permissions.")
            response.code == 404 -> throw IOException("Resource not found. Check the GitLab URL.")
            else -> throw IOException("Request failed with code ${response.code}: ${response.message}")
        }
    }
}
