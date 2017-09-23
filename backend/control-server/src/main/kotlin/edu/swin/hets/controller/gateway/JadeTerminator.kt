package edu.swin.hets.controller.gateway

import jade.content.lang.sl.SLCodec
import jade.content.onto.basic.Action
import jade.content.onto.basic.Result
import jade.core.ContainerID
import jade.domain.FIPANames
import jade.domain.JADEAgentManagement.JADEManagementOntology
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction
import jade.domain.JADEAgentManagement.ShutdownPlatform
import jade.lang.acl.ACLMessage
import jade.proto.AchieveREInitiator
import jade.util.leap.ArrayList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class JadeTerminator: AchieveREInitiator(null, null) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(JadeTerminator::class.java)
    }

    override fun onStart() {
        super.onStart()

        with(myAgent.contentManager) {
            registerLanguage(SLCodec())
            registerOntology(JADEManagementOntology.getInstance())
        }
    }

    override fun prepareRequests(initialRequest: ACLMessage?): Vector<ACLMessage> {
        logger.info("Preparing Shutdown")
        val request = ACLMessage(ACLMessage.REQUEST).apply {
            addReceiver(myAgent.ams)
            ontology = JADEManagementOntology.getInstance().name
            language = SLCodec().name
        }

        val action = Action(myAgent.ams, ShutdownPlatform())
        myAgent.contentManager.fillContent(request, action)

        return Vector<ACLMessage>(1).apply { add(request) }
    }
}