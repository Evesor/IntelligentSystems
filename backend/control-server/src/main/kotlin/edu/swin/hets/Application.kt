package edu.swin.hets

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.controller.distributor.ContainerDistributor
import edu.swin.hets.controller.distributor.LocalContainerDistributor
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import jade.core.ProfileImpl
import jade.core.Runtime
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Application(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Application::class.java.name)

    }

    private val configuration = SystemConfig(args)
    private var containerDistributor = LocalContainerDistributor(Runtime.instance(), ContainerDistributor.DEFAULT_CONTAINER_CONFIGURATION)

    fun start() {
        logger.info("Loading connectionDetails")
        logger.info("Starting JADE deployment server...")
        val jadeController = JadeController(Runtime.instance(), containerDistributor)
        jadeController.start()
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