package edu.swin.hets.controller

import edu.swin.hets.agent.GlobalValuesAgent
import edu.swin.hets.agent.LoggingAgent
import edu.swin.hets.agent.WebAgent
import edu.swin.hets.controller.distributor.ContainerDistributor
import edu.swin.hets.controller.gateway.AgentRetriever
import edu.swin.hets.controller.gateway.ContainerListRetriever
import edu.swin.hets.controller.gateway.JadeTerminator
import edu.swin.hets.web.ClientWebSocketHandler
import jade.core.*
import jade.util.leap.Properties
import jade.wrapper.ContainerController
import jade.wrapper.gateway.JadeGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Responsible for the JADE platform's lifecycle.
 */
class JadeController(private val runtime: Runtime,
                     private val containerDistributor: ContainerDistributor,
                     private val clientWebSocketHandler: ClientWebSocketHandler) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(JadeController::class.java)
    }

    private val profile: Profile = ProfileImpl(true)
    var mainContainer: ContainerController? = null

    init {
        profile.setParameter(Profile.GUI, "true")
    }

    fun start() {
        logger.info("Spinning up the JADE platform...")
        mainContainer = runtime.createMainContainer(profile).also {
            it.createNewAgent("LoggingAgent", LoggingAgent::class.java.name, arrayOf()).start()
            it.createNewAgent("WebServer", WebAgent::class.java.name, arrayOf(clientWebSocketHandler)).start()
            it.createNewAgent("GlobalValues", GlobalValuesAgent::class.java.name, arrayOf()).start()
        }
        JadeGateway.init(null,
                Properties().apply {
                    setProperty(Profile.CONTAINER_NAME, "Gateway")
                    setProperty(Profile.MAIN_HOST, "localhost")
                    setProperty(Profile.MAIN_PORT, "1099")
                })

        Thread.sleep(1000)
        containerDistributor.start()
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