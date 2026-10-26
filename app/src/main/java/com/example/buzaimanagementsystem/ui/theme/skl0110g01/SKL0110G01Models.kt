package com.example.buzaimanagementsystem.ui.theme.skl0110g01

data class HokanInfoResponseModel(
    val success: Boolean,
    val count: Int,
    val hokanBoxCd: String?,
    val hokanBoxMeisho: String?,
    val message: String?
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