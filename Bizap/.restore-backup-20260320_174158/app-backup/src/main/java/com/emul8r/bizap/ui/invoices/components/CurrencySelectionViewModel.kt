package com.emul8r.bizap.ui.invoices.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Currency
import com.emul8r.bizap.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CurrencySelectionViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    
    val currencies: StateFlow<List<Currency>> = 
        currencyRepository.getEnabledCurrencies()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    suspend fun getCurrencySymbol(code: String): String {
        return currencyRepository.getCurrencyByCode(code).first()?.symbol ?: "$"
    }
}
