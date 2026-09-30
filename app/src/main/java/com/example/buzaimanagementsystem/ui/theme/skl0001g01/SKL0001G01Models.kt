package com.example.buzaimanagementsystem.ui.theme.skl0001g01

// 既存のDto
data class SKL0001G01ConditionDto(
    val unyocd: String,
    val moji1: String?,
    val moji2: String?,
    val suji1: String?,
    val suji2: String?
)

data class SKL0001G01UserDto(
    val userCd: String,
    val userName: String,
    val password: String,
    val bushoCd: String,
    val bushoName: String,
    val gymTntCd: String?, // 追加
    val gymTntMei: String?  // 追加
)

data class SKL0001G01LoginStatusDto(
    val isLogined: Boolean,
    val sysName: String
)

// 【修正】パスワードフィールドを追加（ログイン時に画面から入力された値を送るため）
data class SKL0001G01LoginRequest(
    val userCd: String,
    val password: String, // ← ここを追加！
    var userName: String,
    var bushoCd: String,
    var bushoName: String
)

// ==========================================
// 【新しく追加が必要なレスポンス用クラス】
// ==========================================

// 1. GET api/SKL0001G01/conditions のレスポンス用
data class SKL0001G01ConditionsResponse(
    val operationFlag: String?,
    val operationTime: SKL0001G01ConditionDto?
)

// 2. POST api/SKL0001G01/login のレスポンス用
data class SKL0001G01LoginResponse(
    val success: Boolean,
    val message: String?,
    val data: SKL0001G01UserDto?
)