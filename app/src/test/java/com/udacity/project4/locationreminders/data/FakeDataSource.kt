package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError( value:Boolean){
        shouldReturnError = value
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError){
            Result.Error("Reminder not found")
        } else {
            val reminder = reminders?.find{
                it.id == id
            }
            if (reminder != null)
                Result.Success(reminder)
            else Result.Error("Reminder not found")
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("Reminders not found")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }

        return Result.Error("Reminders not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}