package com.udacity.project4.locationreminders.reminderslist

import android.content.res.Resources
import android.view.View
import androidx.core.util.Preconditions.checkState
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeReminderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
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

        launchFragmentInContainer( null, R.style.AppTheme) {
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
        Thread.sleep(5000)

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

        launchFragmentInContainer( null, R.style.AppTheme) {
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
        Thread.sleep(5000)

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

        launchFragmentInContainer( null, R.style.AppTheme) {
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
        Thread.sleep(2000)

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

    @Test
    fun signOut_navigateToAuthentication() {
        // given

        if (FirebaseAuth.getInstance().currentUser != null)
            FirebaseAuth.getInstance().signOut()

        val navController = mock(NavController::class.java)

        // when
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

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // then
        verify(navController).navigate(
            ReminderListFragmentDirections.toAuthentication()
        )
    }

}

/**
 *  Copyright 2018 Danny Roa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

class RecyclerViewMatcher(private val recyclerId: Int) {
    fun atPosition(position: Int): TypeSafeMatcher<View?> {
        return atPositionOnView(position, UNSPECIFIED)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): TypeSafeMatcher<View?> {
        return object : TypeSafeMatcher<View?>() {
            var resources: Resources? = null
            var recycler: RecyclerView? = null
            var holder: RecyclerView.ViewHolder? = null
            override fun describeTo(description: Description) {
                checkState(resources != null, "resource should be init by matchesSafely()")
                if (recycler == null) {
                    description.appendText("RecyclerView with " + getResourceName(recyclerId))
                    return
                }
                if (holder == null) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                if (targetViewId == UNSPECIFIED) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                description.appendText(
                    String.format(
                        "in RecyclerView (%s) at position %s and with %s",
                        getResourceName(recyclerId),
                        position,
                        getResourceName(targetViewId)
                    )
                )
            }

            private fun getResourceName(id: Int): String {
                return try {
                    "R.id." + (resources?.getResourceEntryName(id) ?: "")
                } catch (ex: Resources.NotFoundException) {
                    String.format("resource id %s - name not found", id)
                }
            }

            override fun matchesSafely(view: View?): Boolean {
                if (view != null) {
                    resources = view.resources
                }
                if (view != null) {
                    recycler = view.rootView.findViewById(recyclerId)
                }
                if (recycler == null) return false
                holder = recycler!!.findViewHolderForAdapterPosition(position)
                if (holder == null) return false
                return if (targetViewId == UNSPECIFIED) {
                    view === holder!!.itemView
                } else {
                    view === holder!!.itemView.findViewById<View>(targetViewId)
                }
            }
        }
    }

    companion object {
        const val UNSPECIFIED = -1
    }
}
