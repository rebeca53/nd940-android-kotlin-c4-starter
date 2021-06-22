package com.udacity.project4.authentication

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationViewModelTest : TestCase() {

    @Test
    fun testGetAuthenticationState() {
        // GIVEN
        val authenticationViewModel = AuthenticationViewModel(ApplicationProvider.getApplicationContext())
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        //todo attempt authenticationq

        // WHEN
        val state = authenticationViewModel.authenticationState

        // THEN
        assertEquals(AuthenticationViewModel.AuthenticationState.AUTHENTICATED, state)
    }
}