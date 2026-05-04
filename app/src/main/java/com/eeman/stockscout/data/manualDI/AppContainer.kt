package com.eeman.stockscout.data.manualDI

import android.content.Context
import com.eeman.stockscout.data.db.AppDatabase
import com.eeman.stockscout.data.remote.api.RetrofitClient
import com.eeman.stockscout.data.repo.ItemRepository
import com.eeman.stockscout.data.repo.PickRepository
import com.eeman.stockscout.domain.usecases.PickItemUseCase
import com.eeman.stockscout.domain.usecases.ResolveItemUseCase

class AppContainer(context: Context) {

    private val db = AppDatabase.getInstance(context)

    private val itemDao = db.itemDao()
    private val aliasDao = db.aliasDao()
    private val pendingPickDao = db.pendingPickDao()
    private val apiService = RetrofitClient.apiService

    val itemRepository = ItemRepository(itemDao, aliasDao, apiService)
    val pickRepository = PickRepository(context, itemDao, pendingPickDao)

    val resolveItemUseCase = ResolveItemUseCase(itemRepository)
    val pickItemUseCase = PickItemUseCase(pickRepository)
}