package com.eeman.stockscout.ui.vms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eeman.stockscout.data.models.Item
import com.eeman.stockscout.data.repo.ItemRepository
import com.eeman.stockscout.data.repo.PickRepository
import com.eeman.stockscout.domain.usecases.PickItemUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val item: Item, val pendingCount: Int) : DetailUiState()
    object NotFound : DetailUiState()
}

class ItemDetailViewModel(
    private val itemCode: String,
    private val itemRepository: ItemRepository,
    private val pickRepository: PickRepository,
    private val pickItemUseCase: PickItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _pickDone = MutableStateFlow(false)
    val pickDone: StateFlow<Boolean> = _pickDone.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            // Observe the live item + pending count together
            itemRepository.observeItems().collect { items ->
                val item = items.find { it.itemCode == itemCode }
                if (item == null) {
                    _uiState.value = DetailUiState.NotFound
                } else {
                    pickRepository.pendingCount.collect { count ->
                        _uiState.value = DetailUiState.Success(item, count)
                    }
                }
            }
        }
    }

    fun pick() {
        viewModelScope.launch {
            pickItemUseCase(itemCode)
            _pickDone.value = true
            _pickDone.value = false   // reset so it can fire again
        }
    }

    class Factory(
        private val itemCode: String,
        private val itemRepository: ItemRepository,
        private val pickRepository: PickRepository,
        private val pickItemUseCase: PickItemUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            ItemDetailViewModel(itemCode, itemRepository, pickRepository, pickItemUseCase) as T
    }
}