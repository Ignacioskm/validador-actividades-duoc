package com.validador_actividades_duoc.data.remote

import com.validador_actividades_duoc.domain.model.Post
import retrofit2.http.GET

interface ApiService {
    @GET("/posts")
    suspend fun getPosts(): List<Post>
}