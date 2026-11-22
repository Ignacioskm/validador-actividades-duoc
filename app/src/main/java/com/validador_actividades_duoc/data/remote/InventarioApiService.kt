package com.validador_actividades_duoc.data.remote

import retrofit2.Response
import retrofit2.http.*

interface InventarioApiService {
    @GET("api/recursos")
    suspend fun obtenerRecursos(@Query("departamento") departamento: String? = null): List<RecursoDto>

    @GET("api/recursos/disponibilidad")
    suspend fun consultarDisponibilidad(
        @Query("fecha") fecha: String,
        @Query("bloqueInicio") bloqueInicio: Int,
        @Query("bloqueFin") bloqueFin: Int
    ): List<RecursoDto>

    @POST("api/recursos")
    suspend fun crearRecurso(@Body body: CrearRecursoRequest): RecursoDto

    @PATCH("api/recursos/{id}/stock")
    suspend fun actualizarStock(@Path("id") id: Long, @Body body: ActualizarStockRequest): RecursoDto

    @DELETE("api/recursos/{id}")
    suspend fun desactivarRecurso(@Path("id") id: Long): Response<Unit>

    @POST("api/compromisos")
    suspend fun comprometerStock(@Body body: CompromisoRequest): Response<Unit>

    @PATCH("api/compromisos/{reservaRef}/liberar")
    suspend fun liberarStock(@Path("reservaRef") reservaRef: String): Response<Unit>
}