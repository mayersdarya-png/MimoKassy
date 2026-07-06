package com.mimokassy.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updatePhone(phone: String) {
        _state.value = _state.value.copy(phone = phone)
    }

    fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }

    fun setError(error: String?) {
        _state.value = _state.value.copy(error = error)
    }
}

data class LoginState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isPhoneValid: Boolean
        get() = phone
            .replace("[^0-9+]".toRegex(), "")
            .let { it.length >= 11 && it.startsWith("+") }

    val phoneError: String?
        get() = if (phone.isNotEmpty() && !isPhoneValid) {
            "Похоже, номер введён не полностью"
        } else null
}
