package com.eeman.stockscout.domain.usecases

import com.eeman.stockscout.data.repo.PickRepository

class PickItemUseCase(private val pickRepository: PickRepository) {

    suspend operator fun invoke(itemCode: String) {
        pickRepository.pick(itemCode)
    }
}