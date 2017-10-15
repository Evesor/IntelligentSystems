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
            val containerController: ContainerController = runtime.createAgentContainer(ProfileImpl().apply {
                setParameter(Profile.CONTAINER_NAME, it.name)
            })

            startUpAgents(containerController, it)
        }
    }

    fun startUpAgents(containerController: ContainerController, containerDefinition: ContainerDefinition) {
        val applianceAgents = containerDefinition.agents.filter {
            ApplianceAgent::class.java.isAssignableFrom(Class.forName(it.className))
        }

        val homeAgents = containerDefinition.agents.filter {
            HomeAgent::class.java.isAssignableFrom(Class.forName(it.className))
        }
        //<Home, Appliance>
        val homeAgentMap = mutableMapOf<String, List<String>>()


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

            homeAgentMap.computeIfPresent(appliance.owner, { _, value -> value.plus(appliance.name) })
            homeAgentMap.computeIfAbsent(appliance.owner, { listOf(appliance.name) })
        }

        containerDefinition.agents.forEach { (name, className, arguments) ->
            val argumentList = arguments.split(",").toList()
            val argumentMap: MutableMap<String, Any> = mutableMapOf()
            val ownerList: MutableList<String> = mutableListOf()

            if (className == HomeAgent::class.java.name) {
                ownerList.addAll(homeAgentMap[name] as ArrayList)
            }
            argumentMap.put("Appliances", ownerList)

            containerController.createNewAgent(
                    name,
                    className,
                    arrayOf(argumentList, argumentMap)
            ).start()
        }
    }
}