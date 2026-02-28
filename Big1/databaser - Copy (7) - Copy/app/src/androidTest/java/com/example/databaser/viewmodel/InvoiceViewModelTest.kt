package com.example.databaser.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.databaser.data.AppDatabaseTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class InvoiceViewModelTest : AppDatabaseTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var invoiceViewModel: InvoiceViewModel

    @Before
    override fun createDb() {
        super.createDb()
        Dispatchers.setMain(testDispatcher)
        invoiceViewModel = InvoiceViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeDb()
    }

    @Test
    fun `generateInvoiceNumber should return correct format`() = runTest {
        // Given
        val date = Date()
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val expectedInvoiceNumber = "${dateFormat.format(date)}-1"

        // When
        val actualInvoiceNumber = invoiceViewModel.generateInvoiceNumber()

        // Then
        assertEquals(expectedInvoiceNumber, actualInvoiceNumber)
    }
}