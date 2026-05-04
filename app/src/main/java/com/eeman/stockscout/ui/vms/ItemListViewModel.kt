package com.eeman.stockscout.ui.vms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eeman.stockscout.data.models.Item
import com.eeman.stockscout.data.repo.ItemRepository
import com.eeman.stockscout.domain.usecases.ResolveItemUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ItemListUiState {
    object Loading : ItemListUiState()
    data class Success(val items: List<Item>) : ItemListUiState()
    data class Error(val message: String) : ItemListUiState()
}

sealed class SearchState {
    object Idle : SearchState()
    data class Found(val item: Item) : SearchState()
    object NotFound : SearchState()
}

class ItemListViewModel(
    private val itemRepository: ItemRepository,
    private val resolveItemUseCase: ResolveItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItemListUiState>(ItemListUiState.Loading)
    val uiState: StateFlow<ItemListUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    init {
        syncAndObserve()
    }

    private fun syncAndObserve() {
        viewModelScope.launch {
            // Sync from remote first
            itemRepository.syncFromRemote()   // ignore result — local cache shown regardless

            // Then observe local cache
            itemRepository.observeItems().collect { items ->
                _uiState.value = if (items.isEmpty()) {
                    ItemListUiState.Error("No items found. Check your connection.")
                } else {
                    ItemListUiState.Success(items)
                }
            }
        }
    }

    fun search(input: String) {
        if (input.isBlank()) { _searchState.value = SearchState.Idle; return }
        viewModelScope.launch {
            val item = resolveItemUseCase(input.trim())
            _searchState.value = if (item != null) SearchState.Found(item) else SearchState.NotFound
        }
    }

    fun clearSearch() { _searchState.value = SearchState.Idle }

    class Factory(
        private val itemRepository: ItemRepository,
        private val resolveItemUseCase: ResolveItemUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            ItemListViewModel(itemRepository, resolveItemUseCase) as T
    }
}