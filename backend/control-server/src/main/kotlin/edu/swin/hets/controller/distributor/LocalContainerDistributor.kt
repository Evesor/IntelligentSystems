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

        startUpAgents(systemDefinition)
    }

    fun startUpAgents(systemDefinition: SystemDefinition) {
        val applianceAgents = systemDefinition.containers
                .flatMap { it.agents }
                .filter {
                    ApplianceAgent::class.java.isAssignableFrom(Class.forName(it.className))
                }


        val homeAgents = systemDefinition.containers
                .flatMap { it.agents }
                .filter {
                    HomeAgent::class.java.isAssignableFrom(Class.forName(it.className))
                }

        // Ensure all agents have an owner defined
        applianceAgents.forEach {
            if (it.owner.isBlank()){
                logger.error("${it.name} has no owner!")
                throw IllegalStateException("All appliance agents should have an owner")
            }
        }

        applianceAgents.forEach { appliance ->
            if (homeAgents.none {(name) -> appliance.owner == name} ) {
                logger.error("${appliance.name} does not have a valid owner!")
                throw IllegalStateException("Appliance owner can't be found")
            }
        }

        //<Home, Appliance>
        val homeAgentMap = mutableMapOf<String, List<String>>()

        applianceAgents.forEach { appliance ->
            homeAgentMap.computeIfPresent(appliance.owner, { _, value -> value.plus(appliance.name) })
            homeAgentMap.computeIfAbsent(appliance.owner, { listOf(appliance.name) })
        }

        systemDefinition.containers.forEach { (name, agents) ->
            val containerController: ContainerController = runtime.createAgentContainer(ProfileImpl().apply {
                setParameter(Profile.CONTAINER_NAME, name)
            })

            agents.forEach { (name, className, arguments) ->
                val argumentList = arguments.split(",").toList()
                val argumentMap: MutableMap<String, Any> = mutableMapOf()
                val ownerList: MutableList<String> = mutableListOf()

                containerController.createNewAgent(
                        name,
                        className,
                        arrayOf(argumentList, argumentMap)
                ).start()
            }
        }

        fun startAgent(containerController: ContainerController, agentDefinition: AgentDefinition) {

        }
    }
}