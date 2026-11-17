package com.validador_actividades_duoc.data.repository

import com.validador_actividades_duoc.data.remote.RetrofitInstance
import com.validador_actividades_duoc.domain.model.Post

class PostRepository {
    suspend fun getPosts(): List<Post> {
        return RetrofitInstance.api.getPosts()
    }
}