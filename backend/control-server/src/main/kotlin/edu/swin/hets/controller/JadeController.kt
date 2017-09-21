package edu.swin.hets.controller

import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.util.leap.Properties
import jade.wrapper.ContainerController
import jade.wrapper.gateway.JadeGateway

class JadeController(private val runtime: Runtime) {
    private val profile: Profile = ProfileImpl(true)
    private var mainContainer: ContainerController? = null
    private val gateway: JadeGateway? = null

    init {
        profile.setParameter(Profile.GUI, "true")
    }

    fun start() {
        // TODO: conditional fallback if servers are not able to be connected to
        mainContainer = runtime.createMainContainer(profile)
        JadeGateway.init(null, Properties())
    }

    fun configureAgents() {
        TODO("Detect active servers/dev mode, execute fallback here")
    }

    fun stop() {
        JadeGateway.shutdown()
    }
}