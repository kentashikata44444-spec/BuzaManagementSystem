package com.example.buzaimanagementsystem.ui.theme.skl0101g01

import com.google.gson.annotations.SerializedName

// ロケーション中分類マスタ
data class SltRokeshonchuM(
    @SerializedName(value = "CHUBUNRUICD", alternate = ["chubunruiCd", "ChubunruiCd"])
    val chubunruiCd: String,

    @SerializedName(value = "CHUBUNRUIMEISHO", alternate = ["chubunruiMeisho", "ChubunruiMeisho"])
    val chubunruiMeisho: String?,

    @SerializedName(value = "JOTAIKBN", alternate = ["jotaiKbn", "JotaiKbn"])
    val jotaiKbn: String
)

// ロケーション小分類マスタ
data class SltRokeshonshoM(
    @SerializedName(value = "SHOBUNRUICD", alternate = ["shobunruiCd", "ShobunruiCd"])
    val shobunruiCd: String,

    @SerializedName(value = "SHOBUNRUIMEISHO", alternate = ["shobunruiMeisho", "ShobunruiMeisho"])
    val shobunruiMeisho: String?,

    @SerializedName(value = "JOTAIKBN", alternate = ["jotaiKbn", "JotaiKbn"])
    val jotaiKbn: String
)

// 保管BOX使用情報
data class SltHokanBoxSiyoJoho(
    @SerializedName(value = "HOKANBOXCD", alternate = ["hokanBoxCd", "HokanBoxCd"])
    val hokanBoxCd: String,

    @SerializedName(value = "SEIHINBANGO", alternate = ["seihinBango", "SeihinBango"])
    val seihinBango: String,

    @SerializedName(value = "HOKANSIZAIMEISHO", alternate = ["hokanShizaiMeisho", "HokanShizaiMeisho"])
    val hokanShizaiMeisho: String?,

    @SerializedName(value = "JOTAIKBN", alternate = ["jotaiKbn", "JotaiKbn"])
    val jotaiKbn: String
)

// 保管資材使用情報
data class SltHokanShizaiSiyoJoho(
    @SerializedName(value = "HOKANSIZAICD", alternate = ["hokanShizaiCd", "HokanShizaiCd"])
    val hokanShizaiCd: String,

    @SerializedName(value = "SEIHINBANGO", alternate = ["seihinBango", "SeihinBango"])
    val seihinBango: String,

    @SerializedName(value = "HOKANSIZAIMEISHO", alternate = ["hokanShizaiMeisho", "HokanShizaiMeisho"])
    val hokanShizaiMeisho: String?,

    @SerializedName(value = "JOTAIKBN", alternate = ["jotaiKbn", "JotaiKbn"])
    val jotaiKbn: String
)