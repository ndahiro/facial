package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.biometrics.sdk.FaceBiometricEngine
import com.example.data.model.EnrolledUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FaceVerify SDK", appName)
  }

  @Test
  fun `biometric cosine similarity identical vectors is high`() {
    val vector = FaceBiometricEngine.generateSyntheticFeatures("test-subject-42")
    val similarity = FaceBiometricEngine.calculateCosineSimilarity(vector, vector)
    assertTrue("Similarity should be >= 0.99 for identical vector", similarity >= 0.99f)
  }

  @Test
  fun `biometric gallery search identifies enrolled user above threshold`() {
    val userElena = EnrolledUser(
      id = "USR-01",
      name = "Elena",
      employeeId = "EMP-01",
      department = "R&D",
      role = "Lead",
      accessLevel = "LEVEL_4_ALPHA",
      featureVector = FaceBiometricEngine.generateSyntheticFeatures("Elena-Vector"),
    )
    val userMarcus = EnrolledUser(
      id = "USR-02",
      name = "Marcus",
      employeeId = "EMP-02",
      department = "Security",
      role = "Chief",
      accessLevel = "LEVEL_4_ALPHA",
      featureVector = FaceBiometricEngine.generateSyntheticFeatures("Marcus-Vector"),
    )

    // Probe matches Elena closely
    val probeVector = userElena.featureVector.map { it + 0.001f }
    val result = FaceBiometricEngine.searchGallery(probeVector, listOf(userElena, userMarcus), threshold = 0.75f)

    assertTrue("Search should match", result.isMatch)
    assertEquals("USR-01", result.matchedUser?.id)
  }

  @Test
  fun `anti-spoofing rejects match when active liveness fails`() {
    val userElena = EnrolledUser(
      id = "USR-01",
      name = "Elena",
      employeeId = "EMP-01",
      department = "R&D",
      role = "Lead",
      accessLevel = "LEVEL_4_ALPHA",
      featureVector = FaceBiometricEngine.generateSyntheticFeatures("Elena-Vector"),
    )

    // Probe matches Elena closely, but liveness failed (static photo detected)
    val probeVector = userElena.featureVector
    val result = FaceBiometricEngine.searchGallery(
      probeVector = probeVector,
      gallery = listOf(userElena),
      threshold = 0.75f,
      livenessPassed = false,
      livenessStatus = com.example.biometrics.sdk.LivenessEvaluationResult.FAILED_SPOOF_STATIC,
    )

    assertFalse("Access must be denied when liveness fails", result.isMatch)
    assertEquals(com.example.biometrics.sdk.LivenessEvaluationResult.FAILED_SPOOF_STATIC, result.livenessStatus)
  }

  @Test
  fun `active liveness challenge generator creates valid challenge`() {
    val challenge = FaceBiometricEngine.generateRandomChallenge()
    assertTrue(challenge.type.instruction.isNotEmpty())
    assertTrue(challenge.timeoutSeconds > 0)
  }
}
