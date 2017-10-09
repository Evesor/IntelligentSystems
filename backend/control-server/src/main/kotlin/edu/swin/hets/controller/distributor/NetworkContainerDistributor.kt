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
        private val systemConfig: SystemConfig, //What a mess
        private val connections: List<SlaveConnection>
) : ContainerDistributor(runtime, systemDefinition) {
    /* Maintains a count of the number of containers spun up on that particular connection */
    private val loadBalancingMap: MutableMap<SlaveConnection, Int> = connections
            .associateBy({ it }, { 0 })
            .toMutableMap()

    init {
        if (connections.isEmpty()) {
            throw IllegalStateException("Networked container distributor should not instantiated with an empty list of connections!")
        }

        uploadExecutableToNeededHosts()
    }

    /**
     * Start distributing containers
     */
    override fun start() {
        systemDefinition.containers.forEach {
            val connection = getNextConnection()
            startContainer(connection, it)
            loadBalancingMap.computeIfPresent(connection, { _, value: Int -> value + 1 })
        }
    }

    /**
     * Check if hosts need to upload an executable.
     * Will upload if needed
     */
    private fun uploadExecutableToNeededHosts() {
        logger.info("Checking if hosts require upload...")

        connections.filter { !it.remoteFileExists() }
                .forEach { it.uploadExecutable() }

        logger.info("All remote hosts ready!")
    }

    /**
     * Gets the next connection with the least number of connections.
     *
     * @return the next SlaveConnection with the least number of containers
     */
    private fun getNextConnection(): SlaveConnection {
        return loadBalancingMap
                .minBy { it.value }!!.key
    }

    /**
     * Attempts to start a container on the particular connection.
     *
     * @param connection the connection to start a container on
     * @param containerDefinition the container you want to start
     */
    private fun startContainer(connection: SlaveConnection, containerDefinition: ContainerDefinition) {
        if (connection.startContainer(systemConfig, containerDefinition) != 0) {
            logger.error("Container ${containerDefinition.name} failed to start!")
            //handle this somehow
            throw Exception()
        }
    }
}