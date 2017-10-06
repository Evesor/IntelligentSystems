package edu.swin.hets

import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.controller.distributor.LocalContainerDistributor
import edu.swin.hets.web.ClientWebSocketHandler
import edu.swin.hets.web.WebController
import jade.core.Runtime
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Application(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Application::class.java.name)
    }

    private val configuration = SystemConfig(args)

    //TODO add a DI library
    private val containerDistributor = LocalContainerDistributor(Runtime.instance(), configuration.containerConfiguration)
    private val clientWebSocketHandler = ClientWebSocketHandler()

    private val jadeController = JadeController(Runtime.instance(), containerDistributor, clientWebSocketHandler)
    private val webController = WebController(configuration, jadeController, clientWebSocketHandler)

    fun start() {
        jadeController.start()
        webController.start()
    }
}

fun main(args: Array<String>) {
    val app = Application(args).apply { start() }
}