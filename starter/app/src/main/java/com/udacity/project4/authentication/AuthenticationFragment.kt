package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.databinding.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationFragment : BaseFragment() {
    companion object {
        private const val TAG = "AuthenticationFragment"
        private const val SIGN_IN_RESULT_CODE = 1001
    }

    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentAuthenticationBinding

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_authentication, container, false
        )
        binding.loginButton.setOnClickListener {
            launchSignalInFlow()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        // If the user presses the back button, bring them back to the home screen.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.popBackStack(R.id.reminderListFragment, false)
        }

        // Observe the authentication state so we can know if the user has logged in successfully.
        // If the user has logged in successfully, bring them back to the home screen.
        // If the user did not log in successfully, display an error message.
        _viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                RemindersListViewModel.AuthenticationState.AUTHENTICATED -> navController.popBackStack()
                else -> Log.e(
                    TAG,
                    "Authentication state that doesn't require any UI change $authenticationState"
                )
            }
        })

    }

    // Start firebase login activity to login with email or gmail account
    private fun launchSignalInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(
                    TAG, "Successfully signed in " +
                        "user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }

        }
    }
}