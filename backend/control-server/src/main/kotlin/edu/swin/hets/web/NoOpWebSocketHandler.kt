package edu.swin.hets.web

class NoOpWebSocketHandler : WebSocketHandler {
    override fun broadcast(message: String) {
    }
}