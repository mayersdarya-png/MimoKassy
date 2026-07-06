package com.mimokassy.data.api

import retrofit2.http.*

// ============================================
// Интерфейс для Retrofit
// ============================================

interface AuthApi {

    @POST("auth/request-code")
    suspend fun requestCode(
        @Body request: RequestCodeRequest
    ): RequestCodeResponse

    @POST("auth/verify-code")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): VerifyCodeResponse

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): TokenPairResponse

    @POST("auth/logout")
    suspend fun logout()

    @POST("auth/push-tokens")
    suspend fun registerPushToken(
        @Body request: PushTokenRequest
    )

    @DELETE("auth/push-tokens")
    suspend fun deletePushToken(
        @Body request: PushTokenDeleteRequest
    )
}

// ============================================
// DTOs (Data Transfer Objects)
// ============================================

data class RequestCodeRequest(
    val phone: String
)

data class RequestCodeResponse(
    val ttl_seconds: Int,
    val resend_after_seconds: Int
)

data class VerifyCodeRequest(
    val phone: String,
    val code: String
)

data class VerifyCodeResponse(
    val tokens: TokenPairResponse,
    val client: ClientResponse,
    val is_new: Boolean
)

data class ClientResponse(
    val id: String,
    val name: String?,
    val phone: String,
    val created_at: String
)

data class TokenPairResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int
)

data class RefreshTokenRequest(
    val refresh_token: String
)

data class PushTokenRequest(
    val token: String,
    val platform: String
)

data class PushTokenDeleteRequest(
    val token: String,
    val platform: String
)
