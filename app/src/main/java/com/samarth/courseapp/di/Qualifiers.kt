package com.samarth.courseapp.di

import javax.inject.Qualifier

/**
 * Dispatchers are injected rather than referenced as `Dispatchers.IO` directly, so unit tests can
 * substitute a test dispatcher and stay deterministic.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
