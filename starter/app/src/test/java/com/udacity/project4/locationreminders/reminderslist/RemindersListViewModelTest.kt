package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@SmallTest
class RemindersListViewModelTest : TestCase(){
    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        // todo mock firebase
        reminderDataSource = FakeDataSource()
        val reminder1 = ReminderDTO(
            "Take a picture",
            "Take picture of the beautiful bridge",
            "Blank Bridge",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        val reminder2 = ReminderDTO(
            "Buy a sandwich",
            "Buy sandwich for wife",
            "Subway",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        val reminder3 = ReminderDTO(
            "Start step counter",
            "Start step counter for a walk",
            "Pretty New Park",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        reminderDataSource.addReminders(reminder1, reminder2, reminder3)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @After
    fun after() {
        stopKoin()
    }

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @Test
    fun loadReminders_loadNewAddedReminder() {
        // given
        mainCoroutineRule.dispatcher.pauseDispatcher()
        remindersListViewModel.remindersList.getOrAwaitValue()?.isEmpty()?.let { assertTrue(it) }

        // when
        remindersListViewModel.loadReminders()
        assertTrue(remindersListViewModel.showLoading.getOrAwaitValue() == true)

        mainCoroutineRule.dispatcher.resumeDispatcher()

        assertTrue(remindersListViewModel.showLoading.getOrAwaitValue() == false)

        // then
        assertEquals(reminderDataSource.reminderData.values.toList(), remindersListViewModel.remindersList.value)
        assertEquals(false, remindersListViewModel.showNoData.getOrAwaitValue())
    }


    @Test
    fun loadReminders_failLoadNewAddedReminder() {
        // given
        remindersListViewModel.remindersList.getOrAwaitValue()?.isEmpty()?.let { assertTrue(it) }
        reminderDataSource.setReturnError(true)

        // when
        remindersListViewModel.loadReminders()

        // then
        assertNotSame(reminderDataSource.reminderData.values.toList(), remindersListViewModel.remindersList.value)
        assertEquals(true, remindersListViewModel.showNoData.getOrAwaitValue())
    }
}