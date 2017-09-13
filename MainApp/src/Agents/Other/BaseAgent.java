package Agents.Other;

import Helpers.PowerSaleAgreement;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

/******************************************************************************
 *  Use: An abstract base agent class used to provide all of the default time
 *       keeping functionality that all agents need.
 *  Notes:
 *       - Message handling: Base agent deals with all message que interactions
 *         for an agent. To respond to a message from a sub class of BaseAgent
 *         invoke the register message command with the message template and
 *         the function to handle the message.
 *  Preformatives understood:
 *       - INFORM : Used to tell the agent the next time slice is occurring
 *           - content: "next time now"
 *       - INFORM : Used to tell the agent the next time slice is occurring in
 *                  x amount of time
 *           - content: "next time:X" - "X" int as string in ms
 *       - INFORM_REF : Used to let base know the current time
 *          - inResponseTo: "time"
 *          - content: "time" int as a string
 *  Preformatives Used:
 *       - QUERY_REF : Used to ask the TimeKeeperAgent for the time
 *          - content: "time"
 *****************************************************************************/
public abstract class BaseAgent extends Agent{
    protected int _current_time;
    protected IMessageHandler _time_message_handler;
    private HashMap<MessageTemplate, IMessageHandler> _msg_handlers;

    private MessageTemplate timeMessage = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchContent("next time now"));

    @Override
    protected void setup() {
        super.setup();
        _current_time = getCurrentTimeBlocking();
        _time_message_handler = new HandleTimeMessage();
        _msg_handlers = new HashMap<MessageTemplate, IMessageHandler>();
        _msg_handlers.put(timeMessage, _time_message_handler);
        this.addMessageHandlingBehavior();
    }

    abstract protected void TimeExpiringIn(int expireTimeMS);
    abstract protected void TimeExpired ();
    abstract protected void SaleMade(ACLMessage msg);

    protected void addMessageHandler(MessageTemplate template, IMessageHandler handler) {
        _msg_handlers.put(template, handler);
    }

    // Used to tell someone that you don't understand there message.
    protected void sendNotUndersood(ACLMessage origionalMsg, String content) {
        ACLMessage response = new ACLMessage();
        response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        response.setContent(content);
        response.setInReplyTo(origionalMsg.getReplyWith());
        response.addReceiver(origionalMsg.getSender());
        send(response);
    }

    // Used by the agent at construction to make sure that it get a time at initialization.
    private int getCurrentTimeBlocking() {
        ACLMessage msg = new ACLMessage();
        msg.setPerformative(ACLMessage.QUERY_REF);
        msg.setContent("time");
        msg.setSender(this.getAID());
        msg.setReplyWith("time");
        msg.addReceiver(new AID("TimeKeeper"));
        this.send(msg);
        ACLMessage replyMsg = blockingReceive();
        if ((replyMsg.getInReplyTo().equals("time")) && (replyMsg.getPerformative() == ACLMessage.INFORM_REF)) {
            // We got the correct message, initialize with that time.
            return Integer.parseInt(replyMsg.getContent());
        }
        return 0;
    }

    // Add the message handling to the base class.
    private void addMessageHandlingBehavior () {
        // Deal with any messages to this agent by adding them to the messages vector
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                System.out.println("Called action");
                Iterator keys = _msg_handlers.keySet().iterator();
                while(keys.hasNext()) {
                    MessageTemplate template = (MessageTemplate) keys.next();
                    ACLMessage msg = receive(template);
                    if (msg != null) {
                        // We found a message, pass it to its handler function.
                        System.out.println("Sending message");
                        _msg_handlers.get(template).Handler(msg);
                    }
                }
                block();
            }
        });
    }

    private class HandleTimeMessage implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().contains("next time")) {
                if (msg.getContent().contains("next time now")) {
                    _current_time ++;
                    TimeExpired();
                    System.out.println("Changing time");
                }
                else {
                    String[] num = msg.getContent().split("[[:punct:]]+");
                    TimeExpiringIn(Integer.parseInt(num[num.length - 1]));
                }
            }
        }
    }

    // Method to return list of agents in the platform (taken from AMSDumpAgent from Week3)
    protected AMSAgentDescription[] getAgentList() {
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
