package com.gkprojct.clock

import com.google.gson.*
import java.lang.reflect.Type

class RuleCriteriaAdapter : JsonSerializer<RuleCriteria>, JsonDeserializer<RuleCriteria> {
    companion object {
        private const val TYPE = "type"
        private const val DATA = "data"
    }

    override fun serialize(src: RuleCriteria, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty(TYPE, src::class.java.name)
        jsonObject.add(DATA, context.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RuleCriteria {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get(TYPE).asString
        val data = jsonObject.get(DATA)
        val clazz = Class.forName(type)
        return context.deserialize(data, clazz)
    }
}

class RuleActionAdapter : JsonSerializer<RuleAction>, JsonDeserializer<RuleAction> {
    companion object {
        private const val TYPE = "type"
        private const val DATA = "data"
    }

    override fun serialize(src: RuleAction, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty(TYPE, src::class.java.name)
        jsonObject.add(DATA, context.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RuleAction {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get(TYPE).asString
        val data = jsonObject.get(DATA)
        val clazz = Class.forName(type)
        return context.deserialize(data, clazz)
    }
}
