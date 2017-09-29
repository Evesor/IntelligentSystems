package edu.swin.hets.controller.distributor

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.IntNode
import java.io.IOException

data class AgentDefinition(val name: String, val className: Class<*> , val arguments: String = ""){
    constructor(name: String, className: String, arguments: String) : this(name, Class.forName(className), arguments)
}
data class ContainerDefinition(val name: String, val agents: List<AgentDefinition>)

class ContainerDeserializer @JvmOverloads constructor(valueClass: Class<*>? = null) : StdDeserializer<ContainerDefinition>(valueClass) {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): ContainerDefinition {
        val node = jp.codec.readTree<JsonNode>(jp)
        val id = (node.get("id") as IntNode).numberValue() as Int
        val itemName = node.get("itemName").asText()
        val userId = (node.get("createdBy") as IntNode).numberValue() as Int

        return ContainerDefinition("Placeholder", listOf())
    }
}

interface ContainerDistributor {

}