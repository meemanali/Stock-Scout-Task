package com.eeman.stockscout.domain.usecases

import com.eeman.stockscout.domain.resolver.ItemResolver
import com.eeman.stockscout.data.models.Item
import com.eeman.stockscout.data.repo.ItemRepository

class ResolveItemUseCase(private val itemRepository: ItemRepository) {

    suspend operator fun invoke(input: String): Item? {
        val allItems = itemRepository.getAllItemsDomain()
        val resolver = ItemResolver(allItems)
        return resolver.resolve(input)
    }
}