package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * ログイン画面（SKL0001G01）に関連するAPI通信定義インターフェース
 */
interface SKL0001G01ApiService {

    /**
     * 1. 稼働条件（稼働フラグと稼働時間）をまとめて取得する
     */
    @GET("api/SKL0001G01/conditions")
    suspend fun getConditions(): SKL0001G01ConditionsResponse

    /**
     * 2. ログイン認証、多重チェック、および登録処理をまとめて実行する
     */
    @POST("api/SKL0001G01/login")
    suspend fun login(@Body request: SKL0001G01LoginRequest): SKL0001G01LoginResponse

    /**
     * 3. ログアウトまたはログイン状態の破棄を行う（ユーザーIDをパスパラメータとして送信）
     */
    @DELETE("api/SKL0001G01/login/{userCd}")
    suspend fun deleteLogin(@Path("userCd") userCd: String): SKL0001G01LoginResponse
}