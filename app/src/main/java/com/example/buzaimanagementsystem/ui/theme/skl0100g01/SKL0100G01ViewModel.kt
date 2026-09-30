package com.example.buzaimanagementsystem.ui.theme.skl0100g01

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SKL0100G01ViewModel : ViewModel() {

    var userCd by mutableStateOf("")
        private set
    var gymTntCd by mutableStateOf("")
        private set
    var gymTntMei by mutableStateOf("")
        private set

    // メニュー画面から受け取った基本情報を初期化
    fun initParams(userCd: String, gymTntCd: String, gymTntMei: String) {
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
    }

    fun onButtonClicked(onNavigate: () -> Unit) {
        viewModelScope.launch {
            onNavigate()
        }
    }
}