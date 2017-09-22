package edu.swin.hets.tools

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ShutdownRemotes {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ShutdownRemotes::class.java)
    }

    private var configuration: Configuration = SystemConfig().loadConfig()

    fun terminateRemotes() {
        logger.info("Loading system config...")
        val collection = configuration.getCollection(ConnectionDetails::class.java, SystemConfig.CONNECTION_LIST, null)


        logger.info("Terminating remote instances...")
        collection.stream().forEach({
            SlaveConnection(it, configuration).shutdownJava()
        })
    }
}

fun main(args: Array<String>) {
    ShutdownRemotes().terminateRemotes()
}