package edu.swin.hets.controller.distributor

import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.ContainerController
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LocalContainerDistributor : ContainerDistributor {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(LocalContainerDistributor::class.java)
    }

    val containers: Map<String, ContainerController> = mutableMapOf()

    init {

    }

    fun startContainer(name: String, runtime: Runtime, profile: ProfileImpl) {
        (containers as HashMap<String, ContainerController>).put(
                        name,
                        runtime.createAgentContainer(profile.apply{setParameter(Profile.CONTAINER_NAME, name)})
        )
    }

}