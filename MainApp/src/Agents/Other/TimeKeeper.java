package Agents.Other;

import Helpers.Time;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
/******************************************************************************
 *  Use: A time keep agent that issues time stamps to keep things consent
 *       across containers. Dont message time keeper directly, instead simply
 *       make a new time object.
 *  Preformatives used:
 *       - QUERY_REF : Used to ask for the current time
 *           - content: "time"
 *             - Sends back a -INFORM_REF: with time as an object
 *****************************************************************************/
public class TimeKeeper extends Agent {
    private Time _current_time;

    @Override
    protected void setup() {
        super.setup();
        addResponseBehavior();
    }

    // Add all of our behaviors, only call once at construction.
    private void addResponseBehavior() {
        // Deal with any messages to this agent by adding them to the messages vector
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.QUERY_REF) {
                        if (msg.getContent() == "time") {
                            ACLMessage response = new ACLMessage();
                            response.setPerformative(ACLMessage.INFORM_REF);
                            response.setInReplyTo(msg.getReplyWith());
                            try{
                                response.setContentObject(_current_time);
                            } catch (java.io.IOException e) {}
                            response.addReceiver(msg.getSender());
                        }
                    }
                }
                block();
            }
        });
    }
}
