package edu.swin.hets.controller.gateway;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static edu.swin.hets.agent.NegotiatingAgent.CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY;

public class ChangeBehaviourMessageBehaviour extends OneShotBehaviour {
    private static final Logger logger = LoggerFactory.getLogger(ChangeBehaviourMessageBehaviour.class);
    private String _message;
    private String _agentName;

    public ChangeBehaviourMessageBehaviour(String message) {
        // Get agent name, send the rest to the agent.
        _agentName = message.split(" ")[0];
        List<String> incomingMessage = Arrays.asList(message.split(" ")).subList(1, message.length());
        _message = "";
        for (String s : incomingMessage) {
            message += s;
        }
    }

    public ChangeBehaviourMessageBehaviour(ChangeBehaviourRequest changeBehaviourRequest) {
        _agentName = changeBehaviourRequest.getAgentId();
        _message = changeBehaviourRequest.getMessage();
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology(CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY);
        msg.addReceiver(new AID(_agentName, AID.ISLOCALNAME));
        try {
            msg.setContentObject(_message);
        } catch (IOException e) {
            logger.error(e.toString());
            //TODO, log this as error.
        }
    }
}
