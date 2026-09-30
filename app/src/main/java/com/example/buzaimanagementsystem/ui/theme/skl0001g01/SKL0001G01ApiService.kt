package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SKL0001G01ApiService {

    // 1. 稼働条件（フラグと時間）をまとめて取得
    @GET("api/SKL0001G01/conditions")
    suspend fun getConditions(): SKL0001G01ConditionsResponse

    // 2. ログイン認証・多重チェック・登録をまとめて実行
    @POST("api/SKL0001G01/login")
    suspend fun login(@Body request: SKL0001G01LoginRequest): SKL0001G01LoginResponse

    // ユーザーIDをパスパラメータとして送信しているか確認
    @DELETE("api/SKL0001G01/login/{userCd}")
    suspend fun deleteLogin(@Path("userCd") userCd: String): SKL0001G01LoginResponse
}