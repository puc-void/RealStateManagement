package com.example.realstate.ui.viewmodels

import com.example.realstate.RealStateApp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.User
import com.example.realstate.data.UserRole
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class SignUpSuccess(val userId: String, val role: String) : AuthState()
    data class VerifySuccess(val role: String) : AuthState()
    data class SignInSuccess(val role: UserRole) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun loadSession() {
        val userId = RealStateApp.preferenceManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.getMe()
                if (response.success) {
                    val user = response.data
                    val roleEnum = when (user.role?.uppercase()) {
                        "ADMIN" -> UserRole.ADMIN
                        "AGENT" -> UserRole.AGENT
                        else    -> UserRole.USER
                    }
                    
                    val wId = user.wishlistId ?: user.wishlist?.id ?: ""
                    
                    MockData.currentUser = User(
                        id          = user.id ?: userId,
                        name        = user.name,
                        email       = user.email ?: "",
                        profilePicUrl = if (user.image?.startsWith("data:") == true) "https://i.pravatar.cc/150"
                                        else user.image ?: "https://i.pravatar.cc/150",
                        role        = roleEnum,
                        phone       = user.contactNumber ?: "",
                        location    = user.address ?: "",
                        joinDate    = user.generatedAt?.take(10) ?: "",
                        status      = user.status ?: "active",
                        wishlistId  = wId
                    )

                    if (roleEnum == UserRole.AGENT) {
                        try {
                            val agentsRes = RetrofitClient.agentApi.getAllAgents()
                            if (agentsRes.success) {
                                val myAgent = agentsRes.data.find { it.userId == user.id }
                                if (myAgent != null) {
                                    MockData.currentAgentId = myAgent.id
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signup(
        name: String,
        email: String,
        password: String,
        role: String,
        contactNumber: String = "",
        address: String = "",
        image: String = ""
    ) {
        if (name.length < 3) { _state.update { AuthState.Error("Name must be at least 3 characters") }; return }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { _state.update { AuthState.Error("Enter a valid email address") }; return }
        if (password.length < 6) { _state.update { AuthState.Error("Password must be at least 6 characters") }; return }

        _state.update { AuthState.Loading }
        viewModelScope.launch {
            try {
                val body = buildMap<String, Any> {
                    put("name", name)
                    put("email", email)
                    put("password", password)
                    put("role", role)
                    if (contactNumber.isNotBlank()) put("contactNumber", contactNumber)
                    if (address.isNotBlank()) put("address", address)
                    if (image.isNotBlank()) put("image", image)
                }
                val response = RetrofitClient.authApi.signup(body)
                if (response.success) {
                    val userId = response.data.id ?: ""
                    val token = response.token ?: ""
                    
                    // Save to preferences
                    RealStateApp.preferenceManager.saveToken(token)
                    RealStateApp.preferenceManager.saveUserId(userId)
                    RealStateApp.preferenceManager.saveUserRole(role)

                    val wId = response.data.wishlistId ?: response.data.wishlist?.id ?: ""

                    // Populate initial session
                    MockData.currentUser = User(
                        id = userId,
                        name = name,
                        email = email,
                        role = when (role.uppercase()) {
                            "ADMIN" -> UserRole.ADMIN
                            "AGENT" -> UserRole.AGENT
                            else -> UserRole.USER
                        },
                        profilePicUrl = "https://i.pravatar.cc/150",
                        wishlistId = wId
                    )
                    
                    _state.update { AuthState.SignUpSuccess(userId, role) }
                } else {
                    val msg = response.message.ifBlank { "Sign up failed. This email might already be registered." }
                    _state.update { AuthState.Error(msg) }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("409") == true -> "This email address is already registered."
                    e.message?.contains("400") == true -> "Invalid registration data. Please check your inputs."
                    else -> e.message ?: "Connection error. Please check your internet."
                }
                _state.update { AuthState.Error(errorMsg) }
            }
        }
    }

    fun verifyEmail(userId: String, otp: String, role: String) {
        if (otp.length != 6) { _state.update { AuthState.Error("Enter the 6-digit OTP code") }; return }
        _state.update { AuthState.Loading }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.verificationApi.verifyEmail(userId, mapOf("otp" to otp))
                if (response.success) {
                    // Refresh session data after verification to get the real wishlistId
                    loadSession()
                    _state.update { AuthState.VerifySuccess(role) }
                } else {
                    val msg = if (response.message.contains("expire", true)) "OTP has expired. Please try signing up again."
                             else if (response.message.contains("invalid", true)) "Invalid OTP code. Please check your email."
                             else response.message.ifBlank { "Verification failed. Please try again." }
                    _state.update { AuthState.Error(msg) }
                }
            } catch (e: Exception) {
                _state.update { AuthState.Error(e.message ?: "Verification failed. Please try again.") }
            }
        }
    }

    fun signin(email: String, password: String) {
        if (email.isBlank()) { _state.update { AuthState.Error("Please enter your email") }; return }
        if (password.isBlank()) { _state.update { AuthState.Error("Please enter your password") }; return }

        _state.update { AuthState.Loading }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.signin(mapOf("email" to email, "password" to password))
                if (response.success) {
                    val user = response.data
                    val roleEnum = when (user.role?.uppercase()) {
                        "ADMIN" -> UserRole.ADMIN
                        "AGENT" -> UserRole.AGENT
                        else    -> UserRole.USER
                    }

                    val userId = user.id ?: ""
                    val token = response.token ?: ""
                    RealStateApp.preferenceManager.saveToken(token)
                    RealStateApp.preferenceManager.saveUserId(userId)
                    RealStateApp.preferenceManager.saveUserRole(user.role ?: "USER")

                    val wId = user.wishlistId ?: user.wishlist?.id ?: ""

                    MockData.currentUser = User(
                        id          = user.id ?: userId,
                        name        = user.name,
                        email       = user.email ?: email,
                        profilePicUrl = if (user.image?.startsWith("data:") == true) "https://i.pravatar.cc/150"
                                        else user.image ?: "https://i.pravatar.cc/150",
                        role        = roleEnum,
                        phone       = user.contactNumber ?: "",
                        location    = user.address ?: "",
                        joinDate    = user.generatedAt?.take(10) ?: "",
                        status      = user.status ?: "active",
                        wishlistId  = wId
                    )

                    if (roleEnum == UserRole.AGENT) {
                        try {
                            val agentsRes = RetrofitClient.agentApi.getAllAgents()
                            if (agentsRes.success) {
                                val myAgent = agentsRes.data.find { it.userId == user.id }
                                if (myAgent != null) {
                                    MockData.currentAgentId = myAgent.id
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    _state.update { AuthState.SignInSuccess(roleEnum) }
                } else {
                    _state.update { AuthState.Error(response.message) }
                }
            } catch (e: Exception) {
                _state.update { AuthState.Error(e.message ?: "Sign in failed. Check your credentials.") }
            }
        }
    }

    fun resetState() {
        _state.update { AuthState.Idle }
    }
}
