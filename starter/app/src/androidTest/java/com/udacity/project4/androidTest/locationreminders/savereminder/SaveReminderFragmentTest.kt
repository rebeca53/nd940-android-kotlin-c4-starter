package com.udacity.project4.androidTest.locationreminders.savereminder

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.androidTest.locationreminders.data.local.FakeReminderRepository
import com.udacity.project4.androidTest.util.ToastMatcher
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    private lateinit var repository: ReminderDataSource

    @Before
    fun initRepository() {
        repository = FakeReminderRepository()
        ServiceLocator.remindersRepository = repository
    }

    @After
    fun cleanupDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun addReminder_failToSaveEmptyForm() = runBlockingTest {
        // given
        val navController = Mockito.mock(NavController::class.java)

        launchFragmentInContainer( null, R.style.AppTheme) {
            SaveReminderFragment().also { fragment ->

                // In addition to returning a new instance of our Fragment,
                // get a callback whenever the fragment’s view is created
                // or destroyed so that we can set the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }

        // WHEN

        // check
        onView(withId(R.id.saveReminder)).perform(click())

        // then
//        onView(withText(R.string.error_adding_geofence)).inRoot(ToastMatcher())
//            .check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun addReminder_failToSaveNoLocationForm() = runBlockingTest {
        // given

        val navController = Mockito.mock(NavController::class.java)

        launchFragmentInContainer( null, R.style.AppTheme) {
            SaveReminderFragment().also { fragment ->

                // In addition to returning a new instance of our Fragment,
                // get a callback whenever the fragment’s view is created
                // or destroyed so that we can set the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }
        onView(withId(R.id.reminderTitle)).perform(typeText("Title1"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Description1"))
        closeSoftKeyboard()

        // WHEN
        // check
        onView(withId(R.id.saveReminder)).perform(click())

        // then
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_select_location)))
    }

    @Test
    fun addReminder_fillAndSaveFormNavigateBack() = runBlockingTest {
        // given
        val reminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            37.422160,
            -122.084270
        )

        val navController = Mockito.mock(NavController::class.java)

        launchFragmentInContainer( null, R.style.AppTheme) {
            SaveReminderFragment().also { fragment ->

                // In addition to returning a new instance of our Fragment,
                // get a callback whenever the fragment’s view is created
                // or destroyed so that we can set the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }

                fragment._viewModel.reminderTitle.value = reminder1.title
                fragment._viewModel.reminderDescription.value = reminder1.description
                fragment._viewModel.reminderSelectedLocationStr.value = "Location1"
                fragment._viewModel.latitude.value = reminder1.latitude
                fragment._viewModel.longitude.value = reminder1.longitude
            }
        }

        // WHEN
        // click to save
        onView(withId(R.id.saveReminder)).perform(click())

        // then - navigate back to the reminder list fragment. verify if it has view reminder_location
        onView(withText(R.string.reminder_location)).check(matches(isDisplayed()));
    }

    @Test
    fun selectLocation_navigateToSelectionFragment() = runBlockingTest {
        // given
        val navController = Mockito.mock(NavController::class.java)

        launchFragmentInContainer( null, R.style.AppTheme) {
            SaveReminderFragment().also { fragment ->

                // In addition to returning a new instance of our Fragment,
                // get a callback whenever the fragment’s view is created
                // or destroyed so that we can set the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }
        // WHEN
        // click to save
        onView(withId(R.id.selectLocation)).perform(click())

        // then - navigate to SelectLocation fragment
        Mockito.verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
    }
}
