package edu.swin.hets.controller.agent

import jade.core.Location
import jade.core.Runtime
import jade.wrapper.AgentController
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AgentDistributor {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(AgentDistributor::class.java)
    }

    val agentControllerList: List<AgentController> = mutableListOf()

    fun createNewAgent(location: Location){

    }

}