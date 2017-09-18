package Agents.Other;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

/******************************************************************************
 *  Use: A time keep agent that issues time stamps to keep things consent
 *       across containers. Dont message time keeper directly, instead simply
 *       make a new time object.
 *  Name: Always only have one of these on the main container, have it name set
 *        to "TimeKeeper"
 *  Preformatives understood:
 *       - QUERY_REF : Used to ask for the current time
 *           - content: "time"
 *  Preformatives Used:
 *       - INFORM_REF : Used to send back the current time
 *          - content: "current time"
 *          - content-obj: time as and int
 *       - INFORM : Used to tell the agent the next time slice is occurring
 *           - content: "next time now"
 *       - INFORM : Used to signal that the time will be expiring at some point in the future
 *           - content: "next time in"
 *           - content-obj: next time as double
 *****************************************************************************/
public class TimeKeeperAgent extends Agent {
    private int _current_time;

    @Override
    protected void setup() {
        super.setup();
        _current_time = 0;
        addResponseBehavior();
    }

    // Add all of our behaviors, only call once at construction.
    private void addResponseBehavior() {
        // Deal with any requests for the time.
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.QUERY_REF) {
                        if (msg.getContent().equals("time")) {
                            ACLMessage response = new ACLMessage();
                            response.setPerformative(ACLMessage.INFORM_REF);
                            response.setInReplyTo(msg.getReplyWith());
                            try {
                                response.setContentObject("Test");
                                response.addReceiver(msg.getSender());
                                send(response);
                            } catch (java.io.IOException e) {
                            }
                        }
                    }
                }
                block();
            }
        });
        // Deal with any requests for the time.
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            public void onTick() {
                ACLMessage msg = new ACLMessage();
                msg.setContent("next time now");
                msg.setPerformative(ACLMessage.INFORM);
                AMSAgentDescription[] agents = getAgentList();
                for (AMSAgentDescription agent: agents) {
                    if (agent.getName() != this.getAgent().getAID()) {
                        msg.addReceiver(agent.getName());
                    }
                }
                send(msg);
                _current_time++;
            }
        });
    }

    // Method to return list of agents in the platform (taken from AMSDumpAgent from Week3)
    private AMSAgentDescription[] getAgentList() {
        AMSAgentDescription [] agents = null;
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (new Long(-1));
            agents = AMSService.search( this, new AMSAgentDescription (), c );
        }
        catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
        }
        return agents;
    }

}
