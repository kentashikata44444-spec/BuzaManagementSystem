package com.example.buzaimanagementsystem.ui.theme.skl0110g01

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SKL0110G01ApiService {

    @GET("api/skl0110/hokan-info")
    suspend fun getHokanInfo(
        @Query("seihinBango") seihinBango: String
    ): Response<HokanInfoResponseModel>

    @GET("api/skl0110/master-chu")
    suspend fun getChuMaster(
        @Query("chubunruiCd") chubunruiCd: String
    ): Response<MasterChuResponseModel>

    @GET("api/skl0110/box-info")
    suspend fun getBoxInfo(
        @Query("hokanBoxCd") hokanBoxCd: String
    ): Response<BoxInfoResponseModel>

    @POST("api/skl0110/register")
    suspend fun registerHokanArea(
        @Body request: SKL0111RegisterRequestModel
    ): Response<RegisterResponseModel>
}