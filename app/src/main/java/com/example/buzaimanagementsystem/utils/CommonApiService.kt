package com.example.buzaimanagementsystem.utils

import retrofit2.http.DELETE
import retrofit2.http.Path

interface CommonApiService {
    @DELETE("api/Common/login/{userCd}")
    suspend fun deleteLogin(@Path("userCd") userCd: String)
}