package com.example.buzaimanagementsystem.ui.theme.skl0104g01

import com.google.gson.annotations.SerializedName
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

    @POST("api/skl0104/delete")
    suspend fun deleteData(
        @Body request: DeleteRequestModel
    ): Response<DeleteResponse>
}

data class EmergencyCheckResponse(
    @SerializedName(value = "KINKYU", alternate = ["kinkyu", "Kinkyu"])
    val kinkyu: String?
)

data class DeleteRequestModel(
    @SerializedName(value = "GENPKANRIBANGO", alternate = ["genpkanribango", "Genpkanribango"])
    val genpkanribango: String,

    @SerializedName(value = "SEIHINBANGO", alternate = ["seihinBango", "SeihinBango"])
    val seihinBango: String,

    @SerializedName(value = "HOKANBOXCD", alternate = ["hokanBoxCd", "HokanBoxCd"])
    val hokanBoxCd: String,

    @SerializedName(value = "HOKANSIZAICD", alternate = ["hokanShizaiCd", "HokanShizaiCd"])
    val hokanShizaiCd: String,

    @SerializedName(value = "HASDETAILDATA", alternate = ["hasDetailData", "HasDetailData"])
    val hasDetailData: Boolean
)

data class DeleteResponse(
    @SerializedName(value = "SUCCESS", alternate = ["success", "Success"])
    val success: Boolean
)