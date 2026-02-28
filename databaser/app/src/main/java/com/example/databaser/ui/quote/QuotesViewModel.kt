package com.example.databaser.ui.quote

import androidx.lifecycle.ViewModel
import com.example.databaser.data.dao.QuoteDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val quoteDao: QuoteDao
) : ViewModel() {

    val quotes = quoteDao.getAllQuotes()
}
