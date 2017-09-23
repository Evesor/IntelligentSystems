package edu.swin.hets.controller

import edu.swin.hets.controller.container.ContainerDistributor
import edu.swin.hets.controller.container.LocalContainerDistributor
import edu.swin.hets.controller.gateway.AgentRetriever
import edu.swin.hets.controller.gateway.ContainerListRetriever
import edu.swin.hets.controller.gateway.JadeTerminator
import jade.core.*
import jade.util.leap.Properties
import jade.wrapper.ContainerController
import jade.wrapper.gateway.JadeGateway


class JadeController(private val runtime: Runtime) {
    private val profile: Profile = ProfileImpl(true)
    var mainContainer: ContainerController? = null
    val containerDistributor: ContainerDistributor = LocalContainerDistributor()

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
    }

    fun configureAgents() {
        TODO("Detect active servers/dev mode, execute fallback")
    }

    fun stop() {
        JadeGateway.execute(JadeTerminator())
    }

    fun getContainers(): List<ContainerID> {
        val clr = ContainerListRetriever()
        JadeGateway.execute(clr)
        return clr.getContainerListNative()
    }

    fun getAgentsAtContainer(containerID: ContainerID): List<AID> {
        val ar = AgentRetriever(containerID)
        JadeGateway.execute(ar)
        return ar.getAgentListNative()
    }

    fun getAllAgents(): List<AID> {
        return getContainers()
                .map { getAgentsAtContainer(it) }
                .toList()
                .flatMap { it }
    }
}