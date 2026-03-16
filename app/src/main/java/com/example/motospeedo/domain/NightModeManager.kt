package com.example.motospeedo.domain

import com.example.motospeedo.data.SettingsRepository
import java.util.Calendar

class NightModeManager(private val settings: SettingsRepository) {

    fun computeNightMode(): Boolean {
        if (settings.nightModeEnabled) return true
        if (!settings.autoNightEnabled) return false
        val cal = Calendar.getInstance()
        val nowMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMins = settings.nightStartHour * 60 + settings.nightStartMinute
        val endMins = settings.nightEndHour * 60 + settings.nightEndMinute
        return if (startMins <= endMins) nowMins in startMins..endMins
        else nowMins >= startMins || nowMins <= endMins
    }
}
