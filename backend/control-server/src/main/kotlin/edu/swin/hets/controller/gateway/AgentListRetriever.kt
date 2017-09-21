package edu.swin.hets.controller.gateway

import jade.content.lang.sl.SLCodec
import jade.content.onto.basic.Action
import jade.content.onto.basic.Result
import jade.core.Agent
import jade.core.AgentContainer
import jade.core.ContainerID
import jade.domain.FIPANames
import jade.domain.JADEAgentManagement.JADEManagementOntology
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation
import jade.lang.acl.ACLMessage
import jade.proto.AchieveREInitiator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class AgentListRetriever() : AchieveREInitiator(null, null) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(AgentListRetriever::class.java)
    }

    var agentList: List<Agent>? = null

    override fun onStart() {
        super.onStart()

        with(myAgent.contentManager){
            registerLanguage(SLCodec())
            registerOntology(JADEManagementOntology.getInstance())
        }
    }

    override fun prepareRequests(initialRequest: ACLMessage?): Vector<ACLMessage> {
        val request = ACLMessage(ACLMessage.REQUEST).apply {
            addReceiver(myAgent.ams)
            ontology = JADEManagementOntology.getInstance().name
            language = FIPANames.ContentLanguage.FIPA_SL
        }

        val queryAgentsOnLocation = QueryAgentsOnLocation().apply {
            location = ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null)
        }

        val action = Action(myAgent.ams, queryAgentsOnLocation)
        myAgent.contentManager.fillContent(request, action)
        return Vector<ACLMessage>(1).apply { add(request) }
    }

    override fun handleInform(inform: ACLMessage?) {
        try {
            agentList = (myAgent.contentManager.extractContent(inform) as? Result)?.getValue() as? List<Agent>
        } catch (e: Exception) {
            logger.error(e.toString())
        }
    }
}