package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.LinkedHashMap

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest: ReminderDataSource {

    var reminderData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase
    private val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)


    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()

    @get:Rule
    var instantExecutorRule= InstantTaskExecutorRule()

    @Before
    fun setUpDb() {
        // Create and use an in- memory database which is only used for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    suspend fun refreshReminders() {
        observableReminders.value = getReminders()
    }

    fun observeReminders(): LiveData<Result<List<ReminderDTO>>> {
        runBlocking {
            refreshReminders()
        }
        return observableReminders
    }

    fun observeReminder(reminderId: String): LiveData<Result<ReminderDTO>> {
        runBlocking {
            refreshReminders()
        }
        return observableReminders.map { reminders ->
            when (reminders) {
                is Result.Error -> Result.Error(reminders.message)
                is Result.Success -> {
                    val reminder = reminders.data.firstOrNull() {
                        it.id == reminderId
                    } ?: return@map Result.Error("Not found")
                    Result.Success(reminder)
                }
            }
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(reminderData.values.toList())
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminderData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderData[reminder.id] = reminder
    }

    override suspend fun deleteAllReminders() {
        reminderData.clear()
        refreshReminders()
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            reminderData[reminder.id] = reminder
        }
        runBlocking { refreshReminders() }
    }

    @Test
    fun emptyReminders() = runBlocking {
        repository.deleteAllReminders()

        val result = repository.getReminder(reminder.id)
        assertThat(result is Result.Error, `is`(true))

        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

}