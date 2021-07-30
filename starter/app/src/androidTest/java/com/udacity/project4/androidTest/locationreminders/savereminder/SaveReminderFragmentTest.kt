package com.udacity.project4.androidTest.locationreminders.savereminder

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
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
import com.udacity.project4.androidTest.util.DataBindingIdlingResource
import com.udacity.project4.androidTest.util.monitorFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

    @Before
    fun prepare() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()

        initRepository()
        val myModule = module {
            single {
                SaveReminderViewModel(
                    appContext,
                    repository
                )
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    private fun initRepository() {
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

        val scenario = launchFragmentInContainer<SaveReminderFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // WHEN

        // check
        onView(withId(R.id.saveReminder)).perform(click())

        // then
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun addReminder_failToSaveNoLocationForm() = runBlockingTest {
        // given

        val navController = Mockito.mock(NavController::class.java)

        val scenario = launchFragmentInContainer<SaveReminderFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

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

        val scenario = launchFragmentInContainer<SaveReminderFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

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

        val scenario = launchFragmentInContainer<SaveReminderFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
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
