package com.gkprojct.clock

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter // Import for TypeConverter
import java.util.UUID

// TODO: If RuleCriteria becomes complex, you might need more sophisticated TypeConverters or separate entities.
// For simplicity now, we'll assume RuleCriteria can be stored as a string or similar primitive.
// If RuleCriteria is a sealed class or complex object, you'll need a TypeConverter for it.

// 定义一个 TypeConverter 来处理 UUID 和 Set<Long> 的存储
// Room 默认不支持直接存储 UUID 和 Set<Long>，需要转换成 Room 支持的类型（如 String）
class Converters {
    @TypeConverter
    fun fromUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUuid(uuidString: String?): UUID? {
        return uuidString?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun fromLongSet(set: Set<Long>?): String? {
        return set?.joinToString(",")
    }

    @TypeConverter
    fun toLongSet(setString: String?): Set<Long>? {
        return setString?.split(",")?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    }

    // TODO: Add TypeConverter for RuleCriteria if needed
    // Example: If RuleCriteria is a sealed class, you might store it as a JSON string
    /*
    @TypeConverter
    fun fromRuleCriteria(criteria: RuleCriteria?): String? {
        // You would need a JSON library (like kotlinx.serialization) here
        // return Json.encodeToString(criteria)
        return null // Placeholder
    }

    @TypeConverter
    fun toRuleCriteria(criteriaString: String?): RuleCriteria? {
        // You would need a JSON library (like kotlinx.serialization) here
        // return criteriaString?.let { Json.decodeFromString<RuleCriteria>(it) }
        return null // Placeholder
    }
    */
}


// Room Entity 来表示数据库中的规则表
@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey // 将 id 设置为主键
    val id: UUID, // 使用 UUID 作为规则的唯一标识符

    val name: String,
    val description: String,
    val enabled: Boolean,

    // 使用 TypeConverter 将 Set<Long> 转换为 String 存储
    val targetAlarmIds: Set<UUID>, // 应用到哪些闹钟的 ID
    val calendarIds: Set<Long>,
    val basedOnTime: RuleCriteria.BasedOnTime, // 使用哪些日历进行判断

    // TODO: Add fields for rule criteria details
    // val criteriaType: String, // 存储规则条件的类型 (例如: "AlwaysTrue", "IfCalendarEventExists")
    // val criteriaDetails: String // 存储规则条件的详细信息 (例如: JSON 字符串)

    // TODO: Add other relevant fields (e.g., creation timestamp, last updated timestamp)
)
