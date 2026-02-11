package com.adrencina.enchu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ClientsUiState(
    val isLoading: Boolean = false,
    val filteredClientes: List<Cliente> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<ClientsUiState> = combine(
        repository.getClientes(),
        _searchQuery
    ) { list, query ->
        val filtered = if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.telefono.contains(query) ||
                it.email.contains(query, ignoreCase = true)
            }
        }
        ClientsUiState(
            isLoading = false,
            filteredClientes = filtered,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClientsUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
