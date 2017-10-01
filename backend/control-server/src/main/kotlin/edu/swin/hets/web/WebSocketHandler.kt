package edu.swin.hets.web

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@WebSocket
class WebSocketHandler {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(WebSocketHandler::class.java)
    }

    @Throws(Exception::class)
    @OnWebSocketConnect
    fun onConnect(user: Session) {
        logger.info("${user.localAddress}: websocket connected")
    }

    @Throws(Exception::class)
    @OnWebSocketClose
    fun onClose(user: Session, statusCode: Int, reason: String) {
        logger.info("${user.localAddress}: closed websocket connection")
    }

    @OnWebSocketMessage
    fun onMessage(user: Session, message: String) {
        user.remote.sendString(message)
    }

    @OnWebSocketError
    fun onError(error: Throwable) {
        logger.error(error.toString())
    }
}