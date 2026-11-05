package com.example.buzaimanagementsystem.utils

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// サーバーからのデバイスマッピングレスポンス用DTO（名前に統一を持たせる）
data class DeviceMappingDto(
    val success: Boolean,
    val targetIpAddress: String?,
    val targetMacAddress: String?,
    val targetMeisho: String?
)

interface CommonApiService {
    // 既存のログイン情報削除API
    @DELETE("api/Common/login/{userCd}")
    suspend fun deleteLogin(@Path("userCd") userCd: String)

    // 新しいキャッシュ対応のデバイスマッピング取得API
    @GET("api/barcodereaders/mapping")
    suspend fun getDeviceMapping(
        @Query("ipAddress") ipAddress: String? = null
    ): DeviceMappingDto
}