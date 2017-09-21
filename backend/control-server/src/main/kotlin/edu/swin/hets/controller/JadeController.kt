package edu.swin.hets.controller

import edu.swin.hets.controller.gateway.AgentListRetriever
import jade.core.Agent
import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
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

    fun getAgents(): List<Agent> {
        val alr = AgentListRetriever()
        JadeGateway.execute(alr)
        TODO("FIX ME! ALR returning null regardless")
        return alr.agentList ?: emptyList()
    }
}