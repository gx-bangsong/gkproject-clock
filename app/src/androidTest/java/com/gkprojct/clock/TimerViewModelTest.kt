// 新增Instrumented测试用例
class TimerViewModelTest {
    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testCalendarEventCreation() {
        // 检查日历事件创建流程
        val viewModel = TimerViewModel()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel.createCalendarEvent(context, "测试事件", System.currentTimeMillis(), 3600000)
        // 需要添加具体验证逻辑（如模拟日历插入）
    }

    @Test
    fun testAlarmServiceStart() {
        // 验证服务能否正常启动
        Intent(activityRule.activity, AlarmService::class.java).also { intent ->
            activityRule.activity.startService(intent)
            ShadowAlarmManager.getInstance().cancelAll()
            // 需要使用Shadow类进行模拟测试
        }
    }
}