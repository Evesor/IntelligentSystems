package edu.swin.hets.web


import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.controller.gateway.ChangeBehaviourRequest
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
        staticFiles.location("/public")

        webSocket(ClientWebSocketHandler.PATH, clientWebSocketHandler)

        SparkCorsFilter().apply()
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
                    try {
                        val changeBehaviourRequest =
                                jacksonObjectMapper().readValue(req.body(), ChangeBehaviourRequest::class.java)
                        jadeController.changeBehaviour(changeBehaviourRequest)
                        res.status(200)
                        true
                    } catch (e: Exception) {
                        logger.error(e.toString())
                        logger.error(e.stackTrace.toString())
                        when (e) {
                            is JsonParseException,
                            is JsonMappingException -> {
                                res.status(400)
                            }
                            else -> res.status(500)
                        }
                        false
                    }
                }
            }
        }
    }
}