package com.gkprojct.clock

import com.google.gson.*
import java.lang.reflect.Type

class RuleCriteriaAdapter : JsonSerializer<RuleCriteria>, JsonDeserializer<RuleCriteria> {
    companion object {
        private const val TYPE = "type"
        private const val DATA = "data"
    }

    override fun serialize(src: RuleCriteria?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        if (src == null || context == null) {
            return jsonObject
        }
        jsonObject.addProperty(TYPE, src::class.java.name)
        jsonObject.add(DATA, context.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RuleCriteria {
        if (json == null || context == null) {
            // Return a default or throw an exception
            return RuleCriteria.AlwaysTrue
        }
        val jsonObject = json.asJsonObject
        val type = jsonObject.get(TYPE)?.asString
        val data = jsonObject.get(DATA)

        return try {
            val theClass = Class.forName(type)
            context.deserialize(data, theClass)
        } catch (e: ClassNotFoundException) {
            // Log the error and return a safe default
            e.printStackTrace()
            RuleCriteria.AlwaysTrue
        }
    }
}