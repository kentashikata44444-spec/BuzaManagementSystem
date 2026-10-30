package com.example.buzaimanagementsystem.ui.theme.skl0111g01

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SKL0111G01ApiService {

    /**
     * 1. 部材所在明細情報取得（初期データ検索）
     * パス: GET /api/skl0111/meisai?seihinBango=...
     */
    @GET("api/skl0111/meisai")
    suspend fun getMeisaiInfo(
        @Query("seihinBango") seihinBango: String
    ): Response<SKL0111MeisaiResponseModel>

    /**
     * 2. ロケーション大分類マスタ検索（保管エリア検証）
     * パス: GET /api/skl0111/dai_master?daiCode=...
     */
    @GET("api/skl0111/dai_master")
    suspend fun getDaiMaster(
        @Query("daiCode") daiCode: String
    ): Response<SKL0111DaiMasterResponseModel>

    /**
     * 3. 保管エリア確定・データベース登録更新処理
     * パス: POST /api/skl0111/register
     */
    @POST("api/skl0111/register")
    suspend fun registerHokanArea(
        @Body request: SKL0111RegisterRequestModel
    ): Response<SKL0111RegisterResponseModel>
}