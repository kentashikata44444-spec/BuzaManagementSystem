package com.example.buzaimanagementsystem.ui.theme.skl0002g01

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SKL0002G01ViewModel : ViewModel() {

    var userCd by mutableStateOf("")
        private set

    var userName by mutableStateOf("")
        private set

    var gymTntCd by mutableStateOf("")
        private set

    var gymTntMei by mutableStateOf("")
        private set

    // ユーザー・担当情報を初期化するメソッド
    fun initUserData(userCd: String, userName: String, gymTntCd: String, gymTntMei: String) {
        this.userCd = userCd
        this.userName = userName
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
    }

    fun onShiwakeClicked(onNavigate: () -> Unit) {
        viewModelScope.launch { onNavigate() }
    }

    fun onShuyakuClicked(onNavigate: () -> Unit) {
        viewModelScope.launch { onNavigate() }
    }

    fun onHikiwatashiClicked(onNavigate: () -> Unit) {
        viewModelScope.launch { onNavigate() }
    }

    fun onHenkyakuClicked(onNavigate: () -> Unit) {
        viewModelScope.launch { onNavigate() }
    }

    fun onKobetsuClicked(onNavigate: () -> Unit) {
        viewModelScope.launch { onNavigate() }
    }

    fun onExitClicked(onExit: () -> Unit) {
        viewModelScope.launch { onExit() }
    }
}