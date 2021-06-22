package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.getOrAwaitValue
import junit.framework.TestCase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : TestCase() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        //todo missing data source
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext())
    }

    //TODO: provide testing to the SaveReminderView and its live data objects
    @Test
    fun onClear() {
        saveReminderViewModel.onClear()

        val value = saveReminderViewModel.reminderTitle.getOrAwaitValue()

        assertNull(value)
    }

}