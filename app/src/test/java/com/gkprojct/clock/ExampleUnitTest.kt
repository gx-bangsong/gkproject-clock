// 新增计时器预设测试
@Test
fun testTimerPresets() {
    val viewModel = TimerViewModel()
    assertEquals("泡面", viewModel.presets[0].name)
    assertEquals(180_000, viewModel.presets[0].duration)
}

// 新增时间格式化测试
@Test
fun testTimeFormatting() {
    val viewModel = TimerViewModel()
    val formatted = viewModel.formatTime(3661000)
    assertEquals("01:01:01", formatted)
}