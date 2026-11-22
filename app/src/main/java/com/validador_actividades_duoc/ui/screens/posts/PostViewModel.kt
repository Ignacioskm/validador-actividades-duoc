package com.validador_actividades_duoc.ui.screens.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.remote.RecursoDto
import com.validador_actividades_duoc.data.remote.RetrofitInstance
import com.validador_actividades_duoc.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException

sealed interface PostUiState {
    object Loading : PostUiState
    data class Success(val posts: List<RecursoDto>) : PostUiState
    data class Error(val message: String) : PostUiState
}

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Loading)
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private val _postList = MutableStateFlow<List<RecursoDto>>(emptyList())
    val postList: StateFlow<List<RecursoDto>> = _postList.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            repository.getRecursos()
                .onSuccess { recursos ->
                    _postList.value = recursos
                    _uiState.value = PostUiState.Success(recursos)
                }
                .onFailure { exception ->
                    val errorMsg = when (exception) {
                        is SocketTimeoutException -> "Tiempo de espera agotado al conectar con el servidor backend en ${RetrofitInstance.getBaseUrl()}. Asegúrate de que el servidor Node.js (puerto 3000) esté iniciado y que la IP sea accesible desde tu dispositivo o emulador."
                        is ConnectException -> "No se pudo conectar al servidor backend en ${RetrofitInstance.getBaseUrl()}. Verifica que esté en ejecución en el puerto 3000."
                        else -> exception.localizedMessage ?: "Error al conectar al backend."
                    }
                    _uiState.value = PostUiState.Error(errorMsg)
                }
        }
    }
}