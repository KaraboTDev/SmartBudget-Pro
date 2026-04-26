package com.example.budgetapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetapp.Data.User
import com.example.budgetapp.repository.AuthRepository
import kotlinx.coroutines.launch

// This manager connects your login screens to your database while staying alive even if the user rotates their phone.
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    // Login state
    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    // Registration state - Using Result<Unit> so Activity can handle error messages
    private val _registerResult = MutableLiveData<Result<Unit>?>()
    val registerResult: LiveData<Result<Unit>?> = _registerResult

    // Loading state - disables buttons while DB is working
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.login(username.trim(), password.trim())
            _loginResult.value = user
            _isLoading.value = false
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(username.trim(), password.trim())
            _registerResult.value = result
            _isLoading.value = false
        }
    }

    // Call this after observing the result to prevent it from being triggered again on rotation
    fun clearRegisterResult() {
        _registerResult.value = null
    }
}
