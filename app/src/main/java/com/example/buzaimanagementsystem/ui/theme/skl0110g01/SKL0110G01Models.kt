package com.example.buzaimanagementsystem.ui.theme.skl0110g01

import com.google.gson.annotations.SerializedName

// 保管情報レスポンスモデル（複数件の候補リストに対応）
data class HokanInfoResponseModel(
    val success: Boolean,
    val count: Int,

    // ★ 複数件の保管BOX候補リストを受け取るプロパティを追加
    @SerializedName("data")
    val data: List<HokanBoxItemModel>?,

    val hokanBoxCd: String?,
    val hokanBoxMeisho: String?,
    val message: String?
)

// 保管BOX候補の要素モデル
data class HokanBoxItemModel(
    @SerializedName(value = "hokanBoxCd", alternate = ["HokanBoxCd"])
    val hokanBoxCd: String,

    @SerializedName(value = "hokanBoxMeisho", alternate = ["HokanBoxMeisho"])
    val hokanBoxMeisho: String
)

data class MasterChuResponseModel(
    val success: Boolean,
    val chubunruiMeisho: String?,
    val message: String?
)

data class BoxInfoResponseModel(
    val success: Boolean,
    val hokanBoxMeisho: String?,
    val message: String?
)

data class RegisterResponseModel(
    val success: Boolean,
    val message: String?
)

data class SKL0111RegisterRequestModel(
    val seihinBango: String,
    val hokanBoxCd: String,
    val hokanShizaiCd: String,
    val userCd: String
)