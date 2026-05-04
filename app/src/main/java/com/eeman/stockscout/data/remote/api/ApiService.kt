package com.eeman.stockscout.data.remote.api

import com.eeman.stockscout.data.remote.dto.ItemDto
import com.eeman.stockscout.data.remote.dto.PickRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("items")
    suspend fun getItems(): Response<List<ItemDto>>

    @POST("picks")
    suspend fun postPick(@Body pick: PickRequestDto): Response<Unit>
}