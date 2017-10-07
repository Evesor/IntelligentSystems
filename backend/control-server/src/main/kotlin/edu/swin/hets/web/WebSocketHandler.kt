package edu.swin.hets.web

interface WebSocketHandler {
    fun broadcast(message: String)
}