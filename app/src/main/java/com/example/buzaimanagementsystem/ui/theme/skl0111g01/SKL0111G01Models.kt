package com.example.buzaimanagementsystem.ui.theme.skl0111g01

import com.google.gson.annotations.SerializedName

// 1. 部材所在明細情報レスポンスモデル
data class SKL0111MeisaiResponseModel(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<SKL0111MeisaiItemModel>?
)

data class SKL0111MeisaiItemModel(
    @SerializedName("hokanShizaiCd") val code: String,
    @SerializedName("hokanShizaiMeisho") val name: String
)

// 2. ロケーション大分類マスタ検索レスポンスモデル（倉庫情報・エリア情報を完全取得）
data class SKL0111DaiMasterResponseModel(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("sokoCd") val sokoCd: String?,
    @SerializedName("sokoMeisho") val sokoMeisho: String?,
    @SerializedName("hokanEriaCd") val hokanEriaCd: String?,
    @SerializedName("hokanEriaMeisho") val hokanEriaMeisho: String?
)

// 3. 登録・更新処理レスポンスモデル
data class SKL0111RegisterResponseModel(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<SKL0111MeisaiItemModel>?
)

// 4. 保管エリア確定登録リクエストモデル（C#側 MaterialScanRequestModel と一致させる）
data class SKL0111RegisterRequestModel(
    @SerializedName("seihinBango") val seihinBango: String,
    @SerializedName("hokanShizaiCd") val hokanShizaiCd: String,
    @SerializedName("sokoCd") val sokoCd: String,
    @SerializedName("sokoMeisho") val sokoMeisho: String,
    @SerializedName("hokanEriaCd") val hokanEriaCd: String,
    @SerializedName("hokanEriaMeisho") val hokanEriaMeisho: String,
    @SerializedName("userCd") val userCd: String
)