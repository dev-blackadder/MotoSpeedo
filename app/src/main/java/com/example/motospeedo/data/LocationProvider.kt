package com.example.motospeedo.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor() {

    private val _locationFlow = MutableStateFlow<LocationData?>(null)
    val locationFlow: StateFlow<LocationData?> = _locationFlow

    fun update(data: LocationData) {
        _locationFlow.value = data
    }

    fun clear() {
        _locationFlow.value = null
    }
}
