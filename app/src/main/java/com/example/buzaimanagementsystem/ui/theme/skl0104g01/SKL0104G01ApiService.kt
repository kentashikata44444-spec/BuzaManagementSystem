package com.example.buzaimanagementsystem.ui.theme.skl0104g01

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SKL0104G01ApiService {

    @GET("api/skl0104/emergency-check")
    suspend fun checkEmergency(
        @Query("seihinBango") seihinBango: String
    ): Response<EmergencyCheckResponse>

    @GET("api/skl0104/buhinhyo-info")
    suspend fun getBuhinHyoInfo(
        @Query("seihinBango") seihinBango: String?,
        @Query("buhinKanriNo") buhinKanriNo: String?,
        @Query("hinban") hinban: String?
    ): Response<BuhinHyoResponse>

    @GET("api/skl0104/hokanbox-siyo")
    suspend fun checkHokanBoxSiyo(
        @Query("hokanBoxCd") hokanBoxCd: String
    ): Response<HokanBoxSiyoResponse>

    @POST("api/skl0104/delete")
    suspend fun deleteData(
        @Body request: DeleteRequestModel
    ): Response<DeleteResponse>

    @POST("api/skl0104/register")
    suspend fun registerData(
        @Body request: RegisterRequestModel
    ): Response<RegisterResponse>
}