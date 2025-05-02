package com.gkprojct.clock.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class StopwatchState {
    IDLE, RUNNING, PAUSED
}

data class Lap(val lapNumber: Int, val lapTime: Long, val totalTime: Long)

class StopwatchViewModel : ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _laps = MutableStateFlow<List<Lap>>(emptyList())
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private val _stopwatchState = MutableStateFlow(StopwatchState.IDLE)
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()

    private var timerJob: Job? = null
    private var lastLapTime = 0L

    fun startStopwatch() {
        if (_stopwatchState.value == StopwatchState.RUNNING) return

        _stopwatchState.value = StopwatchState.RUNNING
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - _elapsedTime.value
            while (_stopwatchState.value == StopwatchState.RUNNING) {
                _elapsedTime.value = System.currentTimeMillis() - startTime
                delay(50) // Update roughly every 50ms for smoother display
            }
        }
    }

    fun pauseStopwatch() {
        if (_stopwatchState.value == StopwatchState.RUNNING) {
            _stopwatchState.value = StopwatchState.PAUSED
            timerJob?.cancel()
        }
    }

    fun lapStopwatch() {
        if (_stopwatchState.value != StopwatchState.RUNNING) return

        val currentElapsedTime = _elapsedTime.value
        val lapTime = currentElapsedTime - lastLapTime
        val lapNumber = (_laps.value.firstOrNull()?.lapNumber ?: 0) + 1

        val newLap = Lap(lapNumber = lapNumber, lapTime = lapTime, totalTime = currentElapsedTime)

        _laps.update { currentLaps ->
            listOf(newLap) + currentLaps // Add new lap to the beginning
        }
        lastLapTime = currentElapsedTime
    }

    fun resetStopwatch() {
        _stopwatchState.value = StopwatchState.IDLE
        timerJob?.cancel()
        _elapsedTime.value = 0L
        _laps.value = emptyList()
        lastLapTime = 0L
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // Helper function to format time (MM:SS.ms)
    fun formatTime(timeMillis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (timeMillis % 1000) / 10 // Get hundredths of a second

        return String.format(java.util.Locale.US, "%02d:%02d.%02d", minutes, seconds, milliseconds)
    }
}