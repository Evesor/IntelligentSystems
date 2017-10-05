package edu.swin.hets.controller.distributor

import edu.swin.hets.network.ConnectionDetails
import jade.core.Runtime

class NetworkContainerDistributor(
        runtime: Runtime,
        systemDefinition: SystemDefinition,
        val connections: List<ConnectionDetails>
) : ContainerDistributor(runtime, systemDefinition) {

    init {
        TODO()
    }

    override fun start() {

    }
}