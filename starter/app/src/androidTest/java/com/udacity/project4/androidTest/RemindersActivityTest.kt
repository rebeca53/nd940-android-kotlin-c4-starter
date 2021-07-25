package com.udacity.project4.androidTest

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.udacity.project4.R
import com.udacity.project4.androidTest.util.RecyclerViewMatcher
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun createReminder_createAndDisplayInList(){
        val x = 600
        val y = 1200
        // empty list of reminder
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        // click to add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // fill reminder information
        onView(withId(R.id.reminderTitle)).perform(replaceText("Title1"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description1"))
        onView(withId(R.id.selectLocation)).perform(click())
        Thread.sleep(1500)
        val device: UiDevice = UiDevice.getInstance(getInstrumentation())
        device.swipe(x, y, x, y, 400);
        Thread.sleep(1500)
        onView(withId(R.id.save_location_button)).perform(click())
        Thread.sleep(3500)

        // save reminder
        onView(withId(R.id.saveReminder)).perform(click())
        Thread.sleep(500)

        // back to list. Now it is not empty
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(RecyclerViewMatcher(R.id.reminderssRecyclerView).atPosition(0))
            .check(matches(hasDescendant(withText("Title1"))))
        activityScenario.close()
    }

}
