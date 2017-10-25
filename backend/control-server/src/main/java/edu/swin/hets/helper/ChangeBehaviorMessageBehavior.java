package edu.swin.hets.helper;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.Arrays;
import static edu.swin.hets.agent.NegotiatingAgent.CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY;

public class ChangeBehaviorMessageBehavior extends OneShotBehaviour {
    private String[] _messages;
    private String _agentName;

    public ChangeBehaviorMessageBehavior (String message) {
        _agentName = message.split(" ")[0];
        _messages = (String []) Arrays.asList(message.split(" ")).subList(0, message.length()).toArray();
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology(CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY);
        msg.addReceiver(new AID(_agentName, AID.ISLOCALNAME));
        try {
            msg.setContentObject(_messages);
        } catch (IOException e) {
            //TODO, log this as error.
        }
    }
}
