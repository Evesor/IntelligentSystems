package edu.swin.hets.tools

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MachineProvisioner(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(MachineProvisioner::class.java)
    }

    private val configuration = SystemConfig(args)

    fun provisionHosts() {
        logger.info("Loading system config...")
        val collection = configuration.connectionList

        logger.info("Updating JRE...")
        collection.parallelStream().forEach({
            SlaveConnection(it, configuration).provisionJRE()
        })
    }
}

fun main(args: Array<String>) {
    MachineProvisioner(args).provisionHosts()
}