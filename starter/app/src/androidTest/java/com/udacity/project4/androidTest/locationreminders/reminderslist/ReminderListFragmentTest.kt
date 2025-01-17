package com.udacity.project4.androidTest.locationreminders.reminderslist

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.androidTest.locationreminders.data.local.FakeReminderRepository
import com.udacity.project4.androidTest.util.DataBindingIdlingResource
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.androidTest.util.RecyclerViewMatcher
import com.udacity.project4.androidTest.util.monitorFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

    @Before
    fun prepare() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()

        initRepository()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
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
    fun listReminders_DisplayedInUi() = runBlockingTest {
        // given
        val reminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
        repository.saveReminder(reminder1)

        // WHEN
        val navController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer<ReminderListFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        // then
        // CHeck if recycler has item at position 0true
        onView(withId(R.id.reminderssRecyclerView))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Check item at position 0 has "Title1"
        onView(RecyclerViewMatcher(R.id.reminderssRecyclerView).atPosition(0))
            .check(matches(hasDescendant(withText("Title1"))))
    }

    @Test
    fun noReminders_DisplayedInUi() = runBlockingTest {
        // given

        // WHEN
        val navController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer<ReminderListFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // then
        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun failToLoadReminders_DisplayedInUi() = runBlockingTest {
        // given
        val reminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
        repository.saveReminder(reminder1)
        (repository as FakeReminderRepository).setReturnError(true)

        // WHEN
        val navController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer<ReminderListFragment>( null, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // then
        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickFab_navigateToAddReminder() {
        // given
        val navController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer( null, R.style.AppTheme) {
            ReminderListFragment().also { fragment ->

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
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // when
        onView(withId(R.id.addReminderFAB)).perform(click())

        // then
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }
}