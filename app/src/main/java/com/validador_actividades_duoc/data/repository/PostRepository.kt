package com.validador_actividades_duoc.data.repository

import com.validador_actividades_duoc.data.remote.RecursoDto
import com.validador_actividades_duoc.data.remote.RetrofitInstance

class PostRepository {
    suspend fun getRecursos(): Result<List<RecursoDto>> {
        return runCatching {
            RetrofitInstance.inventarioApi.obtenerRecursos()
        }
    }
}