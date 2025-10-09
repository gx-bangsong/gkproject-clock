package com.gkprojct.clock.vm // <-- **确保包路径正确**

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import java.time.LocalTime
import java.time.DayOfWeek

// --- 导入 RuleCriteria (从 com.gkprojct.clock 包导入) ---
import com.gkprojct.clock.RuleAction
import com.gkprojct.clock.RuleCriteria
// -----------------------------------------------------


// --- Type Converters for Room (Using Gson) ---
// 用于将 RuleCriteria 和 Set<UUID>/Set<Long>/Set<DayOfWeek> 转换为 Room 可以存储的类型
class RuleConverters {
    private val gson = Gson() // <-- This requires the Gson dependency

    @TypeConverter
    fun fromRuleCriteria(criteria: RuleCriteria): String {
        val jsonElement = gson.toJsonTree(criteria)
        if (jsonElement.isJsonObject) {
            val jsonObject = jsonElement.asJsonObject
            jsonObject.addProperty("type", criteria::class.java.simpleName)
            return gson.toJson(jsonObject)
        }
        return gson.toJson(criteria)
    }

    @TypeConverter
    fun toRuleCriteria(criteriaJson: String): RuleCriteria {
        val jsonObject = gson.fromJson(criteriaJson, com.google.gson.JsonObject::class.java)
        val type = jsonObject.get("type")?.asString
        return when (type) {
            "AlwaysTrue" -> RuleCriteria.AlwaysTrue
            "IfCalendarEventExists" -> gson.fromJson(criteriaJson, RuleCriteria.IfCalendarEventExists::class.java)
            "BasedOnTime" -> gson.fromJson(criteriaJson, RuleCriteria.BasedOnTime::class.java)
            "ShiftWork" -> gson.fromJson(criteriaJson, RuleCriteria.ShiftWork::class.java)
            else -> RuleCriteria.AlwaysTrue // Default or error
        }
    }

    @TypeConverter
    fun fromRuleAction(action: RuleAction): String {
        val jsonElement = gson.toJsonTree(action)
        if (jsonElement.isJsonObject) {
            val jsonObject = jsonElement.asJsonObject
            jsonObject.addProperty("type", action::class.java.simpleName)
            return gson.toJson(jsonObject)
        }
        return gson.toJson(action)
    }

    @TypeConverter
    fun toRuleAction(actionJson: String): RuleAction {
        val jsonObject = gson.fromJson(actionJson, com.google.gson.JsonObject::class.java)
        val type = jsonObject.get("type")?.asString
        return when (type) {
            "SkipNextAlarm" -> RuleAction.SkipNextAlarm
            "AdjustAlarmTime" -> gson.fromJson(actionJson, RuleAction.AdjustAlarmTime::class.java)
            else -> RuleAction.SkipNextAlarm // Default or error
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

    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(stringListJson: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringListJson, type)
    }
}
