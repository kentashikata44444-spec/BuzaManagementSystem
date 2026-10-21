package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

import com.google.gson.annotations.SerializedName

data class BarcodeReaderMasterDto(
    @SerializedName("HOZON_NM", alternate = ["hozonNm"])
    val HOZON_NM: String?,

    @SerializedName("MAC_ADS", alternate = ["macAds"])
    val MAC_ADS: String?,

    @SerializedName("MEISHO", alternate = ["meisho"])
    val MEISHO: String?,

    @SerializedName("SEIZOMOTO", alternate = ["seizomoto"])
    val SEIZOMOTO: String?,

    @SerializedName("MODERU_NO", alternate = ["moderuNo"])
    val MODERU_NO: String?,

    @SerializedName("SHIRIARU_NO", alternate = ["shiriaruNo"])
    val SHIRIARU_NO: String?
)

interface SKL0001G01ApiService {

    @GET("api/SKL0001G01/conditions")
    suspend fun getConditions(): SKL0001G01ConditionsResponse

    @POST("api/SKL0001G01/login")
    suspend fun login(@Body request: SKL0001G01LoginRequest): SKL0001G01LoginResponse

    @DELETE("api/SKL0001G01/login/{userCd}")
    suspend fun deleteLogin(@Path("userCd") userCd: String): SKL0001G01LoginResponse

    // ▼ SAT_BCREADER_M マスタ取得用API
    @GET("api/barcodereaders")
    suspend fun getBarcodeReaderList(): List<BarcodeReaderMasterDto>
}