package com.example.buzaimanagementsystem.ui.theme.skl0101g01

import com.google.gson.annotations.SerializedName

// ロケーション中分類マスタ
data class SltRokeshonchuM(
    @SerializedName("CHUBUNRUICD") val chubunruiCd: String,
    @SerializedName("CHUBUNRUIMEISHO") val chubunruiMeisho: String?,
    @SerializedName("JOTAIKBN") val jotaiKbn: String
)

// ロケーション小分類マスタ
data class SltRokeshonshoM(
    @SerializedName("SHOBUNRUICD") val shobunruiCd: String,
    @SerializedName("SHOBUNRUIMEISHO") val shobunruiMeisho: String?,
    @SerializedName("JOTAIKBN") val jotaiKbn: String
)

// 保管BOX使用情報
data class SltHokanBoxSiyoJoho(
    @SerializedName("HOKANBOXCD") val hokanBoxCd: String,
    @SerializedName("SEIHINBANGO") val seihinBango: String,
    @SerializedName("HOKANSIZAIMEISHO") val hokanShizaiMeisho: String?,
    @SerializedName("JOTAIKBN") val jotaiKbn: String
)

// 保管資材使用情報
data class SltHokanShizaiSiyoJoho(
    @SerializedName("HOKANSIZAICD") val hokanShizaiCd: String,
    @SerializedName("SEIHINBANGO") val seihinBango: String,
    @SerializedName("HOKANSIZAIMEISHO") val hokanShizaiMeisho: String?,
    @SerializedName("JOTAIKBN") val jotaiKbn: String
)