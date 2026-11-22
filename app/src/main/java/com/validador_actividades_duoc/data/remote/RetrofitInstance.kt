package com.validador_actividades_duoc.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // http://127.0.0.1:3000/ apunta al localhost de tu PC desde el Emulador de Android Studio.
    // Si usas un dispositivo físico en la misma red Wi-Fi, cambia la URL a la IP local de tu PC (ej: "http://192.168.1.X:3000/")
    private const val DEFAULT_BASE_URL = "http://127.0.0.1:3000/"

    @Volatile
    private var baseUrl: String = DEFAULT_BASE_URL

    @Volatile
    private var currentApi: InventarioApiService? = null

    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        buildOkHttpClient()
    }

    val inventarioApi: InventarioApiService
        get() {
            return currentApi ?: synchronized(this) {
                currentApi ?: createRetrofit(baseUrl).also { currentApi = it }
            }
        }

    fun setBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        synchronized(this) {
            baseUrl = formattedUrl
            currentApi = createRetrofit(formattedUrl)
        }
    }

    fun getBaseUrl(): String = baseUrl

    private fun createRetrofit(url: String): InventarioApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InventarioApiService::class.java)
    }
}