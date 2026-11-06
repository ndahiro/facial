package com.example.data.api

import com.example.data.model.EnrollFaceRequest
import com.example.data.model.EnrollFaceResponse
import com.example.data.model.EnrolledUserDto
import com.example.data.model.HealthResponse
import com.example.data.model.VerifyFaceRequest
import com.example.data.model.VerifyFaceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface FaceVerificationApi {

  @POST("api/v1/verify")
  suspend fun verifyFace(
    @Body request: VerifyFaceRequest,
    @Header("Authorization") authHeader: String,
    @Header("X-Device-Id") deviceId: String,
  ): Response<VerifyFaceResponse>

  @POST("api/v1/enroll")
  suspend fun enrollFace(
    @Body request: EnrollFaceRequest,
    @Header("Authorization") authHeader: String,
  ): Response<EnrollFaceResponse>

  @GET("api/v1/enrolled-users")
  suspend fun getEnrolledUsers(
    @Header("Authorization") authHeader: String,
  ): Response<List<EnrolledUserDto>>

  @GET("api/v1/health")
  suspend fun getHealthStatus(): Response<HealthResponse>
}
