package edu.swin.hets.controller.distributor

import java.io.Serializable

data class AgentDefinition(val name: String, val className: String, val arguments: String = "") : Serializable
data class ContainerDefinition(val name: String, val agents: List<AgentDefinition>) : Serializable
data class SystemDefinition(val containers: List<ContainerDefinition>) : Serializable

interface ContainerDistributor {

}