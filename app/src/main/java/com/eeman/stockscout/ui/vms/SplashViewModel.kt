package com.eeman.stockscout.ui.vms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eeman.stockscout.data.repo.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SplashUiState {
    object Loading : SplashUiState()
    object Done : SplashUiState()
    data class Error(val message: String) : SplashUiState()
}

class SplashViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = itemRepository.syncFromRemote()
            _uiState.value = if (result.isSuccess) {
                SplashUiState.Done
            } else {
                SplashUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    class Factory(private val itemRepository: ItemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            SplashViewModel(itemRepository) as T
    }
}
