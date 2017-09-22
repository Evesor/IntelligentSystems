package edu.swin.hets

import jade.core.Runtime
import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Application {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Application::class.java.name)
    }

    private var configuration: Configuration = SystemConfig().loadConfig()

    fun start() {
        logger.info("Loading connectionDetails")
        logger.info("Starting JADE deployment server...")
        val jadeController = JadeController(Runtime.instance())
        jadeController.start()

        val collection = configuration.getCollection(ConnectionDetails::class.java, SystemConfig.CONNECTION_LIST, null)

        // TODO: conditionally change to local container deployment in dev env
        startUpRemotes(collection)
        //jadeController.configureAgents()
    }

    private fun startUpRemotes(serverList: Collection<ConnectionDetails>) {
        serverList.stream().forEach({
            SlaveConnection(it, configuration).start()
        })
    }
}


fun main(args: Array<String>) {
    val app = Application()
    app.start()
}