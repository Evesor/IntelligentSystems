package edu.swin.hets.web


import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Spark.*

/**
 * Configuration for Web Services API, including websockets.
 */
class WebController(private val systemConfig: SystemConfig,
                    private val jadeController: JadeController,
                    private val clientWebSocketHandler: ClientWebSocketHandler) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WebController::class.java)
    }

    fun start() {
        logger.info("Starting up Web Services...")
        webSocket(ClientWebSocketHandler.PATH, clientWebSocketHandler)
        path("/api") {
            get("/hello") { req, res ->
                "hello world!"
            }

            get("/hostAddress") { req, res ->
                res.status(200)
                systemConfig.hostMachineAddress
            }

            post("/shutdown") { req, res ->
                logger.info("Stop Requested!")
                res.status(200)
                jadeController.stop()
                "done"
            }
        }
    }
}