package edu.swin.hets.web

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketException
import org.eclipse.jetty.websocket.api.annotations.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@WebSocket
class ClientWebSocketHandler: WebSocketHandler {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClientWebSocketHandler::class.java)
        const val PATH: String = "/ws"
    }

    private val sessionList: MutableList<Session> = mutableListOf()

    @Throws(Exception::class)
    @OnWebSocketConnect
    fun onConnect(user: Session) {
        logger.info("${user.localAddress}: websocket connected")
        sessionList.add(user)
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

    override fun broadcast(message: String) {
        val invalidSessions: MutableList<Session> = mutableListOf()
        sessionList.forEach {
            try {
                it.remote.sendString(message)
            } catch (wse: WebSocketException) {
                logger.info(wse.toString())
                logger.info("scheduling to remove connection: ${it.remoteAddress}")
                invalidSessions.add(it)
            }
        }

        //Clean up invalid sessions (is this even necessary?)
        invalidSessions.forEach {
            it.disconnect()
            sessionList.remove(it)
        }
    }
}