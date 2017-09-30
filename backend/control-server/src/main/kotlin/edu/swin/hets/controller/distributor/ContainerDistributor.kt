package edu.swin.hets.controller.distributor

import edu.swin.hets.agent.ApplianceAgent
import edu.swin.hets.agent.HomeAgent
import edu.swin.hets.agent.PowerPlantAgent
import edu.swin.hets.agent.ResellerAgent
import jade.core.Agent
import java.io.Serializable

data class AgentDefinition(val name: String, val className: String, val arguments: String = "") : Serializable
data class ContainerDefinition(val name: String, val agents: List<AgentDefinition>) : Serializable
data class SystemDefinition(val containers: List<ContainerDefinition>) : Serializable

abstract class ContainerDistributor(val systemDefinition: SystemDefinition) {
    companion object {
        val DEFAULT_CONTAINER_CONFIGURATION =
                SystemDefinition(listOf(
                        ContainerDefinition("Power Plant Container", listOf(
                                AgentDefinition("PowerPlant1", PowerPlantAgent::class.java.name),
                                AgentDefinition("PowerPlant2", PowerPlantAgent::class.java.name)
                        )),
                        ContainerDefinition("Appliance Container", listOf(
                                AgentDefinition("Appliance1", ApplianceAgent::class.java.name),
                                AgentDefinition("Appliance2", ApplianceAgent::class.java.name)
                        )),
                        ContainerDefinition("Home Container", listOf(
                                AgentDefinition("Home1", HomeAgent::class.java.name),
                                AgentDefinition("Home2", HomeAgent::class.java.name)
                        )),
                        ContainerDefinition("Reseller Container", listOf(
                                AgentDefinition("Reseller1", ResellerAgent::class.java.name),
                                AgentDefinition("Reseller2", ResellerAgent::class.java.name)
                        ))
                ))
    }


}

fun validateSystemDefinition(systemDefinition: SystemDefinition): Boolean {
    return systemDefinition.containers.stream().allMatch {
        validateContainerDefinition(it)
    }
}

fun validateContainerDefinition(containerDefinition: ContainerDefinition): Boolean {
    return containerDefinition.agents.stream().allMatch {
        validateAgentDefinition(it)
    }
}

fun validateAgentDefinition(agentDefinition: AgentDefinition): Boolean {
    return Agent::class.java.isAssignableFrom(Class.forName(agentDefinition.className))
}
