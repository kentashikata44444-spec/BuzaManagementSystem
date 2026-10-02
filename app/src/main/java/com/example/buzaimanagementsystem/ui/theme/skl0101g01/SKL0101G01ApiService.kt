package com.example.buzaimanagementsystem.ui.theme.skl0101g01

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * SKL0101（保管BOX・保管資材スキャン）画面用のAPIサービスインターフェース
 */
interface SKL0101G01ApiService {

    /**
     * 中分類マスタを取得する
     * @param chubunruiCd 中分類コード
     */
    @GET("api/skl0101/chubunrui")
    suspend fun getChuMaster(
        @Query("chubunruiCd") chubunruiCd: String
    ): Response<SltRokeshonchuM>

    /**
     * 小分類マスタを取得する
     * @param shobunruiCd 小分類コード
     */
    @GET("api/skl0101/shobunrui")
    suspend fun getShoMaster(
        @Query("shobunruiCd") shobunruiCd: String
    ): Response<SltRokeshonshoM>

    /**
     * 保管BOXの使用情報を取得する
     * @param hokanBoxCd 保管BOXコード
     */
    @GET("api/skl0101/hokanboxsiyojoho")
    suspend fun getHokanBoxSiyoJoho(
        @Query("hokanBoxCd") hokanBoxCd: String
    ): Response<SltHokanBoxSiyoJoho>

    /**
     * 保管資材の使用情報を取得する
     * @param hokanShizaiCd 保管資材コード
     */
    @GET("api/skl0101/hokansizaisiyojoho")
    suspend fun getHokanShizaiSiyoJoho(
        @Query("hokanShizaiCd") hokanShizaiCd: String
    ): Response<SltHokanShizaiSiyoJoho>
}