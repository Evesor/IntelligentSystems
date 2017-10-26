package edu.swin.hets.helper;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static edu.swin.hets.agent.NegotiatingAgent.CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY;

public class ChangeBehaviorMessageBehavior extends OneShotBehaviour {
    private String _message;
    private String _agentName;

    public ChangeBehaviorMessageBehavior (String message) {
        // Get agent name, send the rest to the agent.
        _agentName = message.split(" ")[0];
        List<String> incomingMessage = Arrays.asList(message.split(" ")).subList(1, message.length());
        _message = "";
        for (String s : incomingMessage) {
            message += s;
        }
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology(CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY);
        msg.addReceiver(new AID(_agentName, AID.ISLOCALNAME));
        try {
            msg.setContentObject(_message);
        } catch (IOException e) {
            //TODO, log this as error.
        }
    }
}
