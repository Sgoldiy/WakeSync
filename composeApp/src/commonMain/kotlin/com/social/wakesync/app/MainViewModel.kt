package com.social.wakesync.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.wakesync.feature.profile.getProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MainUiState {
    object Initial : MainUiState()
    object Loading : MainUiState()
    object Onboarding1 : MainUiState()
    object Onboarding2 : MainUiState()
    object Onboarding3 : MainUiState()
    object Auth : MainUiState()
    object ProfileSetup : MainUiState()
    object Permissions : MainUiState()
    object Home : MainUiState()
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Initial)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val profileRepository = getProfileRepository()

    fun checkAuthState(isAuthenticated: Boolean, isPermissionsGranted: Boolean) {
        if (!isAuthenticated) {
            if (_uiState.value is MainUiState.Initial || _uiState.value is MainUiState.Loading) {
                _uiState.value = MainUiState.Onboarding1
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val profileResult = profileRepository.getCurrentProfile()
                val profile = profileResult.getOrNull()
                
                if (profile != null && profile.setupCompleted) {
                    if (isPermissionsGranted) {
                        _uiState.value = MainUiState.Home
                    } else {
                        _uiState.value = MainUiState.Permissions
                    }
                } else {
                    _uiState.value = MainUiState.ProfileSetup
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Auth
            }
        }
    }

    fun nextOnboarding() {
        when (_uiState.value) {
            is MainUiState.Onboarding1 -> _uiState.value = MainUiState.Onboarding2
            is MainUiState.Onboarding2 -> _uiState.value = MainUiState.Onboarding3
            is MainUiState.Onboarding3 -> _uiState.value = MainUiState.Auth
            else -> {}
        }
    }

    fun previousOnboarding() {
        when (_uiState.value) {
            is MainUiState.Onboarding2 -> _uiState.value = MainUiState.Onboarding1
            is MainUiState.Onboarding3 -> _uiState.value = MainUiState.Onboarding2
            is MainUiState.Auth -> _uiState.value = MainUiState.Onboarding3
            else -> {}
        }
    }

    fun onAuthenticated(isPermissionsGranted: Boolean) {
        checkAuthState(true, isPermissionsGranted)
    }
    
    fun onProfileCreated() {
        _uiState.value = MainUiState.Permissions
    }
    
    fun onPermissionsGranted() {
        _uiState.value = MainUiState.Home
    }
    
    fun logout() {
        _uiState.value = MainUiState.Auth
    }
    
    fun setOnboarding() {
        _uiState.value = MainUiState.Onboarding1
    }
}
