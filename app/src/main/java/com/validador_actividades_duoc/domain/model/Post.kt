package com.validador_actividades_duoc.domain.model

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)