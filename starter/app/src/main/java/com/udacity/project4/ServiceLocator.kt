package com.udacity.project4

import android.content.Context
import android.provider.DocumentsContract
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private var database: RemindersDatabase? = null
    @Volatile
    var remindersRepository: ReminderDataSource? = null
        @VisibleForTesting set

    private val lock = Any()

    fun provideRemindersRepository(context: Context): ReminderDataSource {
        synchronized(this) {
            return remindersRepository ?: createRemindersRepository(context)
        }
    }

    private fun createRemindersRepository(context: Context): RemindersLocalRepository {


        val newRepo = RemindersLocalRepository(createReminderLocalDataSource(context).reminderDao(), Dispatchers.Main)
        remindersRepository = newRepo
        return newRepo
    }

    private fun createReminderLocalDataSource(context: Context): RemindersDatabase {
        return this.database ?: createDataBase(context)
    }

    private fun createDataBase(context: Context): RemindersDatabase {
        val result = Room.databaseBuilder(
            context,
            RemindersDatabase::class.java, "Reminders.db"
        )
            .build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            remindersRepository = null
        }
    }
}