package com.example.buzaimanagementsystem.ui.theme.skl0111g01

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// バックエンドの SKL0111G01 に対応する API インターフェース
interface SKL0111G01ApiService {

    // 例：製品No.を元に現在の保管情報や登録件数を取得するAPI
    @GET("api/skl0111/hokan-info")
    suspend fun getHokanInfo(
        @Query("seihinBango") seihinBango: String
    ): Response<HokanInfoResponseModel>
}

// レスポンス用のデータモデル
data class HokanInfoResponseModel(
    val count: Int,             // 仕分け登録件数
    val areaName: String?,      // 保管エリア名
    val shizaiName: String?     // 保管資材名
)