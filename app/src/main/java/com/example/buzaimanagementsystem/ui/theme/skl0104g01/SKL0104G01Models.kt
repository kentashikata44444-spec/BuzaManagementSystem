package com.example.buzaimanagementsystem.ui.theme.skl0104g01

import com.google.gson.annotations.SerializedName

// 緊急品チェックレスポンス
data class EmergencyCheckResponse(
    @SerializedName(value = "KINKYU", alternate = ["kinkyu", "Kinkyu", "kinkyukbn", "Kinkyukbn"])
    val kinkyu: String?
)

// 部品表情報レスポンス
data class BuhinHyoResponse(
    @SerializedName(value = "seihinBango", alternate = ["SEIHINBANGO", "seihin_bango", "SeihinBango"])
    val seihinBango: String?,

    @SerializedName(value = "Tehaisu", alternate = ["TEHAISU", "tehai_su", "TehaiSu", "tehaiSu"])
    val tehaisu: Double?,

    @SerializedName(value = "buhinKanriNo", alternate = ["BUHINKANRIBANGO", "buhin_kanri_no", "BuhinKanriNo"])
    val buhinKanriNo: String?
)

// 保管BOX使用情報レスポンス
data class HokanBoxSiyoResponse(
    @SerializedName(value = "seihinBango", alternate = ["SEIHINBANGO", "seihin_bango"])
    val seihinBango: String?
)

// 削除リクエストモデル
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

// 削除レスポンスモデル
data class DeleteResponse(
    @SerializedName(value = "SUCCESS", alternate = ["success", "Success"])
    val success: Boolean,

    // ★ 削除時にも message を受け取れるように追加
    @SerializedName(value = "MESSAGE", alternate = ["message", "Message"])
    val message: String?
)

// 登録リクエストモデル
data class RegisterRequestModel(
    @SerializedName(value = "DENPYOSHUBETSUCD", alternate = ["denpyoShubetsuCd", "DenpyoShubetsuCd"])
    val denpyoShubetsuCd: String,

    @SerializedName(value = "DENPYOBANGO", alternate = ["denpyoBango", "DenpyoBango"])
    val denpyoBango: String,

    @SerializedName(value = "SEIHINBANGO", alternate = ["seihinBango", "SeihinBango"])
    val seihinBango: String,

    @SerializedName(value = "BUHINKANRINO", alternate = ["buhinKanriNo", "BuhinKanriNo"])
    val buhinKanriNo: String,

    @SerializedName(value = "HOKANBOXCD", alternate = ["hokanBoxCd", "HokanBoxCd"])
    val hokanBoxCd: String,

    @SerializedName(value = "HOKANBOXMEISHO", alternate = ["hokanBoxMeisho", "HokanBoxMeisho"])
    val hokanBoxMeisho: String,

    @SerializedName(value = "HOKANSIZAICD", alternate = ["hokanShizaiCd", "HokanShizaiCd"])
    val hokanShizaiCd: String,

    @SerializedName(value = "HOKANSIZAIMEISHO", alternate = ["hokanShizaiMeisho", "HokanShizaiMeisho"])
    val hokanShizaiMeisho: String,

    @SerializedName(value = "KONKAISHIKYUSU", alternate = ["konkaiShikyuSu", "KonkaiShikyuSu"])
    val konkaiShikyuSu: Int,

    @SerializedName(value = "USERCD", alternate = ["userCd", "UserCd"])
    val userCd: String,

    // ★ ここを追加！
    @SerializedName(value = "KINKYUKBN", alternate = ["kinkyuKbn", "Kinkyukbn"])
    val kinkyuKbn: String? = "1"
)

// 登録レスポンスモデル
data class RegisterResponse(
    @SerializedName(value = "SUCCESS", alternate = ["success", "Success"])
    val success: Boolean,

    @SerializedName(value = "MESSAGE", alternate = ["message", "Message"])
    val message: String?
)