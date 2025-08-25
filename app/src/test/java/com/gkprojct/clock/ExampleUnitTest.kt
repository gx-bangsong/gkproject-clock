package com.gkprojct.clock

import com.gkprojct.clock.vm.TimerViewModel
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    // 新增计时器预设测试
    @Test
    fun testTimerPresets() {
        val viewModel = TimerViewModel()
        assertEquals("泡面", viewModel.presets[0].name)
        assertEquals(180_000, viewModel.presets[0].durationMillis)
    }
}