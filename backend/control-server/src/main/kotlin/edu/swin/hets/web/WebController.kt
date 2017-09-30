package edu.swin.hets.web


import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.kotlin.Http
import spark.kotlin.ignite

class WebController(val systemConfig: SystemConfig, val jadeController: JadeController) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WebController::class.java)
    }

    private val http: Http = ignite()
    fun start() {
        http.get("/hello") {
            logger.info("Hello world!")
            status(200)
            "Hello world!"
        }

        http.get("/hostAddress") {
            status(200)
            systemConfig.hostMachineAddress
        }

        http.post("/shutdown") {
            logger.info("Stop Requested!")
            jadeController.stop()
            "done"
        }

    }
}