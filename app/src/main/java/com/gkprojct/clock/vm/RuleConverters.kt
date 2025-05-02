package com.gkprojct.clock.vm // <-- **确保包路径正确**

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import java.time.LocalTime
import java.time.DayOfWeek

// --- 导入 RuleCriteria (从 com.gkprojct.clock 包导入) ---
import com.gkprojct.clock.RuleCriteria
// -----------------------------------------------------


// --- Type Converters for Room (Using Gson) ---
// 用于将 RuleCriteria 和 Set<UUID>/Set<Long>/Set<DayOfWeek> 转换为 Room 可以存储的类型
class RuleConverters {
    private val gson = Gson() // <-- This requires the Gson dependency

    @TypeConverter
    fun fromRuleCriteria(criteria: RuleCriteria): String {
        return gson.toJson(criteria)
    }

    @TypeConverter
    fun toRuleCriteria(criteriaJson: String): RuleCriteria {
        // This is a simplified deserialization. For complex sealed classes,
        // a custom JsonDeserializer might be needed to correctly identify
        // the specific subclass (AlwaysTrue, IfCalendarEventExists, etc.).
        // For now, we attempt to deserialize into known types.
        return try {
            gson.fromJson(criteriaJson, RuleCriteria.AlwaysTrue::class.java) as? RuleCriteria.AlwaysTrue
                ?: gson.fromJson(criteriaJson, RuleCriteria.IfCalendarEventExists::class.java) as? RuleCriteria.IfCalendarEventExists
                ?: gson.fromJson(criteriaJson, RuleCriteria.BasedOnTime::class.java) as? RuleCriteria.BasedOnTime
                // TODO: Add deserialization for other criteria types here
                ?: RuleCriteria.AlwaysTrue // Default if deserialization fails
        } catch (e: Exception) {
            e.printStackTrace()
            RuleCriteria.AlwaysTrue // Return default on error
        }
    }

    @TypeConverter
    fun fromUuidSet(uuidSet: Set<UUID>): String {
        return gson.toJson(uuidSet)
    }

    @TypeConverter
    fun toUuidSet(uuidSetJson: String): Set<UUID> {
        val type = object : TypeToken<Set<UUID>>() {}.type
        return gson.fromJson(uuidSetJson, type)
    }

    @TypeConverter
    fun fromLongSet(longSet: Set<Long>): String {
        return gson.toJson(longSet)
    }

    @TypeConverter
    fun toLongSet(longSetJson: String): Set<Long> {
        val type = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(longSetJson, type)
    }

    @TypeConverter
    fun fromDayOfWeekSet(dayOfWeekSet: Set<DayOfWeek>): String {
        return gson.toJson(dayOfWeekSet)
    }

    @TypeConverter
    fun toDayOfWeekSet(dayOfWeekSetJson: String): Set<DayOfWeek> {
        val type = object : TypeToken<Set<DayOfWeek>>() {}.type
        return gson.fromJson(dayOfWeekSetJson, type)
    }
}
