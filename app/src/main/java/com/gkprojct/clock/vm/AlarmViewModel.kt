package com.gkprojct.clock.vm

import androidx.lifecycle.*
import com.gkprojct.clock.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class AlarmViewModel(private val alarmDao: AlarmDao) : ViewModel() {

    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms().map { list ->
        list.map { entity ->
            val calendar = Calendar.getInstance().apply { timeInMillis = entity.timeInMillis }
            Alarm(
                id = entity.id,
                time = calendar,
                label = entity.label,
                isEnabled = entity.isEnabled,
                repeatingDays = entity.repeatingDays,
                sound = entity.sound,
                vibrate = entity.vibrate,
                appliedRules = entity.appliedRules
            )
        }
    }

    fun saveAlarm(alarm: Alarm) = viewModelScope.launch {
        val entity = AlarmEntity(
            id = alarm.id,
            timeInMillis = alarm.time.timeInMillis,
            label = alarm.label,
            isEnabled = alarm.isEnabled,
            repeatingDays = alarm.repeatingDays,
            sound = alarm.sound,
            vibrate = alarm.vibrate,
            appliedRules = alarm.appliedRules
        )
        alarmDao.insertAlarm(entity)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        val entity = AlarmEntity(
            id = alarm.id,
            timeInMillis = alarm.time.timeInMillis,
            label = alarm.label,
            isEnabled = alarm.isEnabled,
            repeatingDays = alarm.repeatingDays,
            sound = alarm.sound,
            vibrate = alarm.vibrate,
            appliedRules = alarm.appliedRules
        )
        alarmDao.deleteAlarm(entity)
    }
}

class AlarmViewModelFactory(private val alarmDao: AlarmDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(alarmDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
