package edu.swin.hets

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.controller.distributor.LocalContainerDistributor
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import edu.swin.hets.web.WebController
import jade.core.Runtime
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Application(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Application::class.java.name)
    }

    private val configuration = SystemConfig(args)
    private val containerDistributor = LocalContainerDistributor(Runtime.instance(), configuration.containerConfiguration)
    private val jadeController = JadeController(Runtime.instance(), containerDistributor)
    private val webController = WebController(configuration, jadeController)

    fun start() {
        jadeController.start()
        webController.start()
    }

    private fun startUpRemotes(serverList: Collection<ConnectionDetails>) {
        serverList.stream().forEach({
            SlaveConnection(it, configuration).start()
        })
    }
}


fun main(args: Array<String>) {
    val app = Application(args).also { it.start() }
}