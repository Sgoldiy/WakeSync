package com.social.wakesync.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _usernameStatus = MutableStateFlow<UsernameStatus>(UsernameStatus.Idle)
    val usernameStatus = _usernameStatus.asStateFlow()

    private val repository = getProfileRepository()

    fun saveProfile(username: String, avatarEmoji: String, selectedGoal: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            val result = repository.saveProfile(username, avatarEmoji, selectedGoal)
            
            if (result.isSuccess) {
                _uiState.value = ProfileUiState.Success
            } else {
                _uiState.value = ProfileUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun checkUsername(username: String) {
        if (username.length < 3) {
            _usernameStatus.value = UsernameStatus.Idle
            return
        }

        viewModelScope.launch {
            _usernameStatus.value = UsernameStatus.Checking
            
            val result = repository.checkUsername(username)
            if (result.isSuccess) {
                val isAvailable = result.getOrNull() ?: false
                if (isAvailable) {
                    _usernameStatus.value = UsernameStatus.Available
                } else {
                    _usernameStatus.value = UsernameStatus.Taken
                }
            } else {
                // If there's an error (e.g. no internet), we'll gracefully fallback or stay checking
                // but let's idle it so they can try again.
                _usernameStatus.value = UsernameStatus.Idle
            }
        }
    }
}

sealed class UsernameStatus {
    object Idle : UsernameStatus()
    object Checking : UsernameStatus()
    object Available : UsernameStatus()
    object Taken : UsernameStatus()
}

sealed class ProfileUiState {
    object Initial : ProfileUiState()
    object Loading : ProfileUiState()
    object Success : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
