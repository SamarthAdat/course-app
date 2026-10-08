package com.samarth.courseapp.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Debug-only switch that makes the mock API fail on demand.
 *
 * Error states are the easiest thing to ship broken, because reproducing a 500 by hand is awkward.
 * A toggle in the debug build means the failure path gets exercised on every run.
 */
@Singleton
class MockApiConfig @Inject constructor() {

    private val _forceFailure = MutableStateFlow(false)
    val forceFailure: StateFlow<Boolean> = _forceFailure.asStateFlow()

    fun toggleForceFailure() = _forceFailure.update { !it }
}
