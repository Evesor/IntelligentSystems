package edu.swin.hets.controller.distributor

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.network.SlaveConnection
import jade.core.Runtime

/**
 * A Container Distributor to distribute containers to remote hosts via SSH
 *
 * @param runtime the JADE runtime instance
 * @param systemDefinition the structure of the JADE agent system
 * @param connections the list of connections to spin up containers on
 */
class NetworkContainerDistributor(
        runtime: Runtime,
        systemDefinition: SystemDefinition,
        private val systemConfig: SystemConfig, //TODO untangle this mess pls
        private val connections: List<SlaveConnection>
) : ContainerDistributor(runtime, systemDefinition) {

    init {
        connections.filter { !it.remoteFileExists() }
                .forEach { it.uploadExecutable() }
    }

    override fun start() {
        //TODO figure out load balancing strategy
        connections.forEach {

        }
    }

    fun startContainer(connection: SlaveConnection, containerDefinition: ContainerDefinition) {
        if (connection.startContainer(systemConfig, containerDefinition) != 0){
            logger.error("Container ${containerDefinition.name} failed to start!")
            //handle this somehow
            throw Exception()
        }
    }
}