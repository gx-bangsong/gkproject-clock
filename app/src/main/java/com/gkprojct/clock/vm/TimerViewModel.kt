package com.gkprojct.clock.vm

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Keep only one definition with durationMillis
data class TimerPreset(val name: String, val durationMillis: Long)

enum class TimerState {
    STOPPED,
    RUNNING,
    PAUSED
 }

class TimerViewModel : ViewModel() {
    private var job = Job()
    private var timerJob: Job? = null

    // State for the raw input string (HHMMSS)
    private val _timeInput = MutableStateFlow("000000")
    val timeInput: StateFlow<String> = _timeInput

    internal val _remainingTime = MutableStateFlow(0L) // 毫秒
    val remainingTime: StateFlow<Long> = _remainingTime

    internal val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState

    private var initialDurationMillis: Long = 0L
    // Expose initial duration as StateFlow
    internal val _initialDuration = MutableStateFlow(0L)
    val initialDuration: StateFlow<Long> = _initialDuration

    val presets = listOf(
        TimerPreset("泡面", 180_000),
        TimerPreset("煮蛋", 300_000),
        TimerPreset("冥想", 900_000)
    )

    // --- Input Handling --- 

    fun appendDigit(digit: String) {
        if (_timerState.value != TimerState.STOPPED) return // Only allow input when stopped
        val currentInput = _timeInput.value
        if (digit == "00" && currentInput == "000000") return // Ignore leading double zeros

        val newInput = (currentInput + digit).takeLast(6) // Append and keep last 6 digits
        _timeInput.value = newInput.padStart(6, '0') // Ensure it's always 6 digits
    }

    fun deleteDigit() {
        if (_timerState.value != TimerState.STOPPED) return
        val currentInput = _timeInput.value
        if (currentInput == "000000") return // Cannot delete from zero

        // Shift digits to the right, inserting '0' at the start
        val newInput = "0" + currentInput.dropLast(1)
        _timeInput.value = newInput
    }

    fun setInputTime(durationMillis: Long) {
        if (_timerState.value != TimerState.STOPPED) return
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        // Format to HHMMSS, ensuring it doesn't exceed 99 hours
        val formattedInput = String.format(java.util.Locale.US, "%02d%02d%02d",
            hours.coerceAtMost(99), // Limit hours to 99
            minutes,
            seconds
        )
        _timeInput.value = formattedInput.takeLast(6) // Ensure it's 6 digits
    }

    // --- Timer Control --- 

    fun startTimerFromInput() {
        val input = _timeInput.value
        if (input == "000000" || _timerState.value != TimerState.STOPPED) return

        val hours = input.substring(0, 2).toLongOrNull() ?: 0
        val minutes = input.substring(2, 4).toLongOrNull() ?: 0
        val seconds = input.substring(4, 6).toLongOrNull() ?: 0

        val durationMillis = (hours * 3600 + minutes * 60 + seconds) * 1000
        if (durationMillis > 0) {
            startTimer(durationMillis)
        }
    }

    // Original startTimer logic, now private or internal if only called internally
    private fun startTimer(durationMillis: Long) {
        if (_timerState.value == TimerState.RUNNING) return

        if (_timerState.value == TimerState.STOPPED) {
            this.initialDurationMillis = durationMillis
            this._initialDuration.value = durationMillis // Set initial duration state
            _remainingTime.value = durationMillis
        }

        _timerState.value = TimerState.RUNNING
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - (initialDurationMillis - _remainingTime.value)
            while (_timerState.value == TimerState.RUNNING) {
                val elapsed = System.currentTimeMillis() - startTime
                _remainingTime.value = (initialDurationMillis - elapsed).coerceAtLeast(0L)
                if (_remainingTime.value == 0L) {
                    stopTimer() // Stop automatically when time reaches zero
                    // TODO: Add notification or sound feedback here
                    break
                }
                delay(50) // Update more frequently for smoother display
            }
        }
    }

    fun addMinute() {
        if (_timerState.value == TimerState.RUNNING || _timerState.value == TimerState.PAUSED) {
            val newRemainingTime = _remainingTime.value + 60000L
            _remainingTime.value = newRemainingTime

            // Also update initial duration so progress calculation remains correct
            _initialDuration.value += 60000L

            // If paused and time was 0, we need to potentially restart the timer logic
            if (_timerState.value == TimerState.PAUSED && _remainingTime.value > 0 && timerJob?.isCompleted == true) {
                // The timer loop finished, but we added time while paused.
                // We might need to re-initiate the timer logic upon resuming.
                // For now, just updating the time is sufficient. Resuming will handle it.
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
        }
    }

    // Resume timer function
    fun resumeTimer() {
        if (_timerState.value == TimerState.PAUSED) {
            startTimer(_initialDuration.value) // Restart the timer logic with the current initial duration
        }
    }

    fun resetTimer() {
        stopTimer()
        _timeInput.value = "000000" // Reset input field
    }

    private fun stopTimer() {
        _timerState.value = TimerState.STOPPED
        timerJob?.cancel()
        _remainingTime.value = 0L
        _initialDuration.value = 0L // Reset initial duration when fully stopped
        // Don't reset _timeInput here, resetTimer handles that
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun createCalendarEvent(context: Context, title: String, startTime: Long, duration: Long) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + duration)
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, "手机闹钟")
            .putExtra(CalendarContract.Events.DESCRIPTION, "由Clock应用创建的闹钟事件")
            .putExtra(CalendarContract.Events.ALL_DAY, false)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "未找到日历应用", Toast.LENGTH_SHORT).show()
        }
    }
}