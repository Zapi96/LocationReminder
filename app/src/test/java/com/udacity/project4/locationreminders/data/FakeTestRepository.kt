package com.udacity.project4.locationreminders.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking


class FakeTestRepository : ReminderDataSource {

    var reminderData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()

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
        return Result.Error("could not find task")
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
}