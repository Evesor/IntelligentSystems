package edu.swin.hets.web


import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Spark.*

/**
 * Configuration for Web Services API, including websockets.
 */
class WebController(
        private val systemConfig: SystemConfig,
        private val jadeController: JadeController,
        private val clientWebSocketHandler: ClientWebSocketHandler
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WebController::class.java)
    }

    fun start() {
        logger.info("Starting up Web Services...")
        port(if (!systemConfig.devMode) 80 else 4567)
        webSocket(ClientWebSocketHandler.PATH, clientWebSocketHandler)
        staticFiles.location("/public")
        path("/api") {
            before("/*") { req, _ -> logger.info("received API call from ${req.ip()}") }
            post("/shutdown") { req, res ->
                logger.info("JADE shutdown requested!")
                res.status(200)
                jadeController.stop()
                "done"
            }
            path("/agent") {
                post("/change-behaviour") { req, res ->
                    res.status(200)
                }
            }
        }
    }
}