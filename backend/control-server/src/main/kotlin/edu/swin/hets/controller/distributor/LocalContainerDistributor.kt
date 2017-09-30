package edu.swin.hets.controller.distributor

import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.ContainerController
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LocalContainerDistributor(runtime: Runtime, systemDefinition: SystemDefinition) : ContainerDistributor(runtime, systemDefinition) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(LocalContainerDistributor::class.java)
    }

    override fun start() {
        logger.info("Starting up secondary containers...")
        systemDefinition.containers.forEach {
            val containerController = runtime.createAgentContainer(ProfileImpl().apply{
                setParameter(Profile.CONTAINER_NAME, it.name)
            })

            it.agents.forEach{ (name, className) ->
                containerController.createNewAgent(name, className, arrayOf())
            }
        }
    }
}