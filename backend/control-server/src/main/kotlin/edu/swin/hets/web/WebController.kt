package edu.swin.hets.web


import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Spark.*

class WebController(private val systemConfig: SystemConfig, private val jadeController: JadeController) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WebController::class.java)
    }

    fun start() {
        webSocket("/ws", WebSocketHandler::class.java)
        path("/api") {
            get("/hello") { req, res ->
                "hello world!"
            }

            get("/api/hello") { req, res ->
                logger.info("Hello world!")
                res.status(200)
                "Hello world!"
            }

            get("/api/hostAddress") { req, res ->
                res.status(200)
                systemConfig.hostMachineAddress
            }

            post("/api/shutdown") { req, res ->
                logger.info("Stop Requested!")
                res.status(200)
                jadeController.stop()
                "done"
            }
        }
    }
}