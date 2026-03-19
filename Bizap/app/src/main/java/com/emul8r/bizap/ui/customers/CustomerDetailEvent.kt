package com.emul8r.bizap.ui.customers

sealed interface CustomerDetailEvent {
    object CustomerDeleted : CustomerDetailEvent
    object CustomerUpdated : CustomerDetailEvent
}
