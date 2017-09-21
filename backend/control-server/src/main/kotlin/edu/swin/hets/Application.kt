package edu.swin.hets

import jade.core.Runtime
import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Application (val args: Array<String>){
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Application::class.java.name)
        const val DEV_MODE_ARG: String = "--dev"
    }

    private var devMode: Boolean = args.contains(DEV_MODE_ARG)
    private var configuration: Configuration = SystemConfig().loadConfig()

    fun start() {
        logger.info("Loading connectionDetails")
        logger.info("Starting JADE deployment server...")
        val jadeController = JadeController(Runtime.instance())
        jadeController.start()

        val collection: Collection<ConnectionDetails>? = configuration.getCollection(ConnectionDetails::class.java, SystemConfig.CONNECTION_LIST, null)

        if (!devMode && collection != null)
            startUpRemotes(collection)
        else
            TODO("Dev mode not implemented")
        //jadeController.configureAgents()
    }

    private fun startUpRemotes(serverList: Collection<ConnectionDetails>) {
        serverList.stream().forEach({
            SlaveConnection(it, configuration).start()
        })
    }
}


fun main(args: Array<String>) {
    val app = Application(args)
    app.start()
}