package edu.swin.hets.controller.distributor

import edu.swin.hets.agent.ApplianceAgent
import edu.swin.hets.agent.HomeAgent
import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.ContainerController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class LocalContainerDistributor(
        runtime: Runtime,
        systemDefinition: SystemDefinition
) : ContainerDistributor(runtime, systemDefinition) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LocalContainerDistributor::class.java)
    }

    override fun start() {
        logger.info("Starting up secondary containers...")
        systemDefinition.containers.forEach {
            val containerController = runtime.createAgentContainer(ProfileImpl().apply {
                setParameter(Profile.CONTAINER_NAME, it.name)
            })

            it.agents.forEach { (name, className, arguments) ->
                containerController.createNewAgent(
                        name,
                        className,
                        arrayOf(arguments.split(",").toList())
                ).start()
            }
        }
    }

    fun startUpAgents(containerController: ContainerController, agentDefinition: List<AgentDefinition>) {
        val applianceAgents = agentDefinition.filter {
            ApplianceAgent::class.java.isAssignableFrom(Class.forName(it.className))
        }

        val homeAgents = agentDefinition.filter {
            HomeAgent::class.java.isAssignableFrom(Class.forName(it.className))
        }

        // Ensure all agents have an owner defined
        if (applianceAgents.any { it.owner.isBlank() }) {
            throw IllegalStateException("All appliance agents should have an owner!")
        }

        // Ensure that all agents have a valid owner
        if (applianceAgents.any { (name) ->
            homeAgents.none { name == it.name }
        }) {
            throw IllegalStateException("Appliance owner can't be found!")
        }

        while (!(applianceAgents as Stack).empty()) {
            val appliance = applianceAgents.pop()

//            val owner = agentDefinition.first { appliance.owner == it.name }
        }

    }
}