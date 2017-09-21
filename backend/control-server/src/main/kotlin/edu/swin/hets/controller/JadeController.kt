package edu.swin.hets.controller

import edu.swin.hets.controller.gateway.AgentListRetriever
import jade.core.*
import jade.util.leap.Properties
import jade.wrapper.ContainerController
import jade.wrapper.gateway.JadeGateway


class JadeController(private val runtime: Runtime) {
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
                    setProperty(Profile.MAIN_HOST, "localhost")
                    setProperty(Profile.MAIN_PORT, "1099")
                })
    }

    fun configureAgents() {
        TODO("Detect active servers/dev mode, execute fallback")
    }

    fun stop() {
        JadeGateway.shutdown()
    }

    /**
     * Runs an agent to collect list of agents.
     * @return List of type AID of all running agents
     */
    fun getAgents(): List<AID> {
        val alr = AgentListRetriever()
        JadeGateway.execute(alr)
        return alr.getAgentListNative()
    }


    fun getContainers(): List<ContainerID> {
        TODO()
    }
}