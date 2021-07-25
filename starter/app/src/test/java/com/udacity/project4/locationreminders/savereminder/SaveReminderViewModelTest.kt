package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.TestCase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class SaveReminderViewModelTest : TestCase() {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel


    @Before
    fun setupViewModel() {
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
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun onClear() {
        //when
        saveReminderViewModel.onClear()

        // then
        assertNull(saveReminderViewModel.reminderTitle.getOrAwaitValue())
        assertNull(saveReminderViewModel.reminderDescription.getOrAwaitValue())
        assertNull(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue())
        assertNull(saveReminderViewModel.selectedMarker.getOrAwaitValue())
        assertNull(saveReminderViewModel.latitude.getOrAwaitValue())
        assertNull(saveReminderViewModel.longitude.getOrAwaitValue())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun validateAndSaveReminder_saveValid() {
        // given
        val reminder = ReminderDataItem(
            "Title1",
            null,
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE)

        // when
        saveReminderViewModel.validateAndSaveReminder(reminder)

        // then
        assertTrue(reminderDataSource.reminderData.contains(reminder.id))

        val reminderSaved = ReminderDTO(
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.id
        )
        assertEquals(reminderSaved, reminderDataSource.reminderData[reminder.id])

        assertFalse(saveReminderViewModel.showLoading.getOrAwaitValue())
        assertEquals("Reminder Saved !", saveReminderViewModel.showToast.getOrAwaitValue())
        assertEquals(NavigationCommand.Back, saveReminderViewModel.navigationCommand.getOrAwaitValue())
    }

    @Test
    fun validateAndSaveReminder_saveInvalidTitle() {
        //given
        val reminder = ReminderDataItem(
            null,
            "Description1",
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE)

        //when
        saveReminderViewModel.validateAndSaveReminder(reminder)

        //then
        assertFalse(reminderDataSource.reminderData.contains(reminder.id))
        assertNotSame(reminderDataSource.reminderData[reminder.id], reminder)
//        assertEquals(R.string.err_enter_title, saveReminderViewModel.showSnackBar.getOrAwaitValue())
    }

    @Test
    fun validateAndSaveReminder_saveInvalidLocation() {
        // given
        val reminder = ReminderDataItem(
            "Title1",
            "Description1",
            null,
            Double.MAX_VALUE,
            Double.MAX_VALUE)

        // when
        saveReminderViewModel.validateAndSaveReminder(reminder)

        // then
        assertFalse(reminderDataSource.reminderData.contains(reminder.id))
        assertNotSame(reminderDataSource.reminderData[reminder.id], reminder)
//        assertEquals(R.string.err_select_location, saveReminderViewModel.showSnackBar.getOrAwaitValue())

    }

}