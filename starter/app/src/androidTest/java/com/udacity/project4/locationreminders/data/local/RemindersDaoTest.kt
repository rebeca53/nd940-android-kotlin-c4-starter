package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertTrue

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

// Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // given
        val reminder = ReminderDTO(
        "Title1",
        "Description1",
        "Location1",
        Double.MAX_VALUE,
        Double.MAX_VALUE)
        database.reminderDao().saveReminder(reminder)

        // when
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // then
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveManyReminderAndGetAll() = runBlockingTest {
        // given
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
            database.reminderDao().saveReminder(reminder)
        }

        // when
        val allReminders = database.reminderDao().getReminders()

        // then
        for (index in 0..allReminders.lastIndex) {
            val loaded = allReminders[index]
            val reminder = listReminders[index]
            assertThat<ReminderDTO>(loaded, notNullValue())
            assertThat(loaded.id, `is`(reminder.id))
            assertThat(loaded.description, `is`(reminder.description))
            assertThat(loaded.location, `is`(reminder.location))
            assertThat(loaded.latitude, `is`(reminder.latitude))
            assertThat(loaded.longitude, `is`(reminder.longitude))
        }
    }

    @Test
    fun deleteAll() = runBlockingTest {
        // given
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
            database.reminderDao().saveReminder(reminder)
        }
        val allReminders = database.reminderDao().getReminders()
        assertThat(allReminders, notNullValue())

        // when
        database.reminderDao().deleteAllReminders()

        // then
        val remainingReminder = database.reminderDao().getReminders()
        assertTrue(remainingReminder.isEmpty())
    }

}