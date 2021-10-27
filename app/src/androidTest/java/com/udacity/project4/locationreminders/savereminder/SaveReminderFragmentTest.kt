package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.util.monitorSaveReminderFragment
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito

class SaveReminderFragmentTest: KoinTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var viewModel: SaveReminderViewModel

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()
        val myModule = module {
            viewModel {
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(), get() as ReminderDataSource)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }

        }
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }
        repository = get()

        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), repository)


        runBlocking {
            repository.deleteAllReminders()
        }
    }



    @Test
    fun noTitle(){
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)

        val navController = Mockito.mock(NavController::class.java)

        dataBindingIdlingResource.monitorSaveReminderFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(click())
        Espresso.onView(ViewMatchers.withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))
    }




}