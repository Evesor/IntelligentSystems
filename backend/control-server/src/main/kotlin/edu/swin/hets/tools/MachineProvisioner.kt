package edu.swin.hets.tools

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MachineProvisioner() {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(MachineProvisioner::class.java)
    }

    private var configuration: Configuration = SystemConfig().loadConfig()

    fun provisionHosts() {
        logger.info("Loading system config...")
        val collection = configuration.getCollection(ConnectionDetails::class.java, SystemConfig.CONNECTION_LIST, null)

        logger.info("Updating JRE...")
        collection.parallelStream().forEach({
            SlaveConnection(it, configuration).provisionJRE()
        })
    }
}

fun main(args: Array<String>) {
    MachineProvisioner().provisionHosts()
}