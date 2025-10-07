package com.gkprojct.clock.vm

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.gkprojct.clock.RuleCriteria
import com.gkprojct.clock.RuleCriteriaAdapter
import java.util.UUID
import java.time.DayOfWeek

class RuleConverters {
    // Configure Gson with the custom adapter for handling the sealed class hierarchy
    private val gson = GsonBuilder()
        .registerTypeAdapter(RuleCriteria::class.java, RuleCriteriaAdapter())
        .create()

    @TypeConverter
    fun fromRuleCriteria(criteria: RuleCriteria?): String? {
        if (criteria == null) {
            return null
        }
        return gson.toJson(criteria, RuleCriteria::class.java)
    }

    @TypeConverter
    fun toRuleCriteria(criteriaJson: String?): RuleCriteria? {
        if (criteriaJson.isNullOrEmpty()) {
            return null
        }
        return gson.fromJson(criteriaJson, RuleCriteria::class.java)
    }

    @TypeConverter
    fun fromUuidSet(uuidSet: Set<UUID>?): String? {
        return uuidSet?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toUuidSet(uuidSetJson: String?): Set<UUID> {
        if (uuidSetJson.isNullOrEmpty()) {
            return emptySet()
        }
        val type = object : TypeToken<Set<UUID>>() {}.type
        return gson.fromJson(uuidSetJson, type)
    }

    @TypeConverter
    fun fromLongSet(longSet: Set<Long>?): String? {
        return longSet?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toLongSet(longSetJson: String?): Set<Long> {
        if (longSetJson.isNullOrEmpty()) {
            return emptySet()
        }
        val type = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(longSetJson, type)
    }

    @TypeConverter
    fun fromDayOfWeekSet(dayOfWeekSet: Set<DayOfWeek>?): String? {
        return dayOfWeekSet?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDayOfWeekSet(dayOfWeekSetJson: String?): Set<DayOfWeek> {
        if (dayOfWeekSetJson.isNullOrEmpty()) {
            return emptySet()
        }
        val type = object : TypeToken<Set<DayOfWeek>>() {}.type
        return gson.fromJson(dayOfWeekSetJson, type)
    }
}