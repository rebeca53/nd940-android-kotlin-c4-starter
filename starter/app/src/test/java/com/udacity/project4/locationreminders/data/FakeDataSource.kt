package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var shouldReturnError = false
    var reminderData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private val observableReminder = MutableLiveData<Result<List<ReminderDTO>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        return Result.Success(reminderData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminderData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find reminder")
    }

    override suspend fun deleteAllReminders() {
        reminderData.clear()
        observableReminder.value = getReminders()
    }

    fun addReminders(vararg  reminders: ReminderDTO) {
        for (reminder in reminders) {
            reminderData[reminder.id] = reminder
        }
        runBlocking {
            observableReminder.value = getReminders()
        }
    }


}