package com.cleon.lokerku.awan.helper.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleon.lokerku.awan.helper.preference.UserPreference
import com.cleon.lokerku.awan.helper.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository, private val userPreference: UserPreference) :
    ViewModel() {

    fun login(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.login(email, password)
            if (result.isSuccess) {
                val username = result.getOrNull()
                if (username != null) {
                    userPreference.saveUsername(username)
                    onSuccess(username)
                } else {
                    onError("Failed to retrieve username")
                }
            } else {
                onError(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
}
