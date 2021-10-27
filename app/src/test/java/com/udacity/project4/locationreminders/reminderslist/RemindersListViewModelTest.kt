package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.FakeTestRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@SmallTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    private val reminder = ReminderDTO(
        "Title1",
        "Description",
        "Location",
        0.0,
        0.0)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val remindersList = mutableListOf<ReminderDTO>()

    @Before
    fun setupViewModel(){
        stopKoin()
        val app = getApplicationContext<MyApp>()
        dataSource = FakeDataSource(remindersList)
        remindersListViewModel = RemindersListViewModel(app, dataSource)

    }

    @Test
    fun loadReminders(){
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()?.size, `is`(1))
    }

    @Test
    fun notfoundReminder() = mainCoroutineRule.runBlockingTest {
        dataSource.setShouldReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.value, `is`("Reminders not found"))
    }
}