package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.buzaimanagementsystem.ui.theme.BuzaiManagementSystemTheme
import com.example.buzaimanagementsystem.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SKL0001G01Activity : ComponentActivity() {

    // もし ViewModel に Factory が必要な場合の構成例、または既存のインスタンス化方法に合わせてください
    private val viewModel: SKL0001G01ViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiService = retrofit.create(SKL0001G01ApiService::class.java)
                @Suppress("UNCHECKED_CAST")
                return SKL0001G01ViewModel(apiService) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuzaiManagementSystemTheme {
                SKL0001G01Screen(
                    viewModel = viewModel,
                    onCloseApp = {
                        finish() // アプリを閉じる
                    },
                    // ▼ 4つの引数を受け取るように修正
                    onLoginSuccess = { userCd, userName, gymTntCd, gymTntMei ->
                        // ログイン成功時の処理
                        // 例: インテント等でメニュー画面へ userCd, gymTntCd, gymTntMei を渡す
                        // val intent = Intent(this, SKL0002G01Activity::class.java).apply {
                        //     putExtra("USER_CD", userCd)
                        //     putExtra("GYM_TNT_CD", gymTntCd)
                        //     putExtra("GYM_TNT_MEI", gymTntMei)
                        // }
                        // startActivity(intent)
                    }
                )
            }
        }
    }
}