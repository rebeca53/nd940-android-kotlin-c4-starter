package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process    }
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        reminderLocalRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanup() {
        database.close()
        stopKoin()
    }

    @Test
    fun saveReminder_getReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val reminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        database.reminderDao().saveReminder(reminder)
        reminderLocalRepository.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID
        val result = reminderLocalRepository.getReminder(reminder.id)

        // THEN - Same reminder is returned
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveManyReminders_getAll() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val reminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        val reminder2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        val reminder3 = ReminderDTO(
            "Title3",
            "Description3",
            "Location3",
            Double.MAX_VALUE,
            Double.MAX_VALUE)
        val listReminders = arrayListOf(reminder1, reminder2, reminder3)
        for (reminder in listReminders) {
            reminderLocalRepository.saveReminder(reminder)
        }

        // WHEN  - Reminder retrieved by ID
        val result = reminderLocalRepository.getReminders()

        // THEN - Same reminder is returned
        result as Result.Success
        for (index in 0..result.data.lastIndex) {
            val loaded = result.data[index]
            val reminder = listReminders[index]
            assertThat<ReminderDTO>(loaded, CoreMatchers.notNullValue())
            assertThat(loaded.id, `is`(reminder.id))
            assertThat(loaded.description, `is`(reminder.description))
            assertThat(loaded.location, `is`(reminder.location))
            assertThat(loaded.latitude, `is`(reminder.latitude))
            assertThat(loaded.longitude, `is`(reminder.longitude))
        }
    }

    @Test
    fun deleteAll_getReminders() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val reminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
        val reminder2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
        val reminder3 = ReminderDTO(
            "Title3",
            "Description3",
            "Location3",
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
        val listReminders = arrayListOf(reminder1, reminder2, reminder3)
        for (reminder in listReminders) {
            reminderLocalRepository.saveReminder(reminder)
        }
        val allReminders = reminderLocalRepository.getReminders()
        assertThat(allReminders, notNullValue())

        // WHEN  - Reminder retrieved by ID
        reminderLocalRepository.deleteAllReminders()

        // THEN - Same reminder is returned
        val result = reminderLocalRepository.getReminders()
        result as Result.Success
        assertTrue(result.data.isEmpty())
    }
}