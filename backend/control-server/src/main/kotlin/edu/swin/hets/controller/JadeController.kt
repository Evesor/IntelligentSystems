package edu.swin.hets.controller

import edu.swin.hets.controller.distributor.ContainerDistributor
import edu.swin.hets.controller.distributor.LocalContainerDistributor
import edu.swin.hets.controller.gateway.AgentRetriever
import edu.swin.hets.controller.gateway.ContainerListRetriever
import edu.swin.hets.controller.gateway.JadeTerminator
import jade.core.*
import jade.util.leap.Properties
import jade.wrapper.ContainerController
import jade.wrapper.gateway.JadeGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class JadeController(private val runtime: Runtime, private val containerDistributor: ContainerDistributor) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(JadeController::class.java)
    }

    private val profile: Profile = ProfileImpl(true)
    var mainContainer: ContainerController? = null


    init {
        profile.setParameter(Profile.GUI, "true")
    }

    fun start() {
        // TODO: conditional fallback if servers are not able to be connected to
        mainContainer = runtime.createMainContainer(profile)
        JadeGateway.init(null,
                Properties().apply {
                    setProperty(Profile.CONTAINER_NAME, "Gateway")
                    setProperty(Profile.MAIN_HOST, "localhost")
                    setProperty(Profile.MAIN_PORT, "1099")
                })

        containerDistributor.start()
    }

    fun configureAgents() {
        TODO("Detect active servers/dev mode, execute fallback")
    }

    fun stop() {
        JadeGateway.execute(JadeTerminator())
    }

    fun getContainers(): List<ContainerID> =
            ContainerListRetriever().let {
                JadeGateway.execute(it)
                it.getContainerListNative()
            }

    fun getAgentsAtContainer(containerID: ContainerID): List<AID> =
            AgentRetriever(containerID).let {
                JadeGateway.execute(it)
                it.getAgentListNative()
            }

    fun getAllAgents(): List<AID> =
            getContainers()
                    .map { getAgentsAtContainer(it) }
                    .toList()
                    .flatMap { it }

}