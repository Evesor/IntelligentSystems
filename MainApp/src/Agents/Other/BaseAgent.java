package Agents.Other;

import Helpers.GlobalValues;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.*;
import Helpers.Weather;
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
 *           - content: "next time in"
 *           - content-obj: next time as double
 *       - INFORM : Used to let the agent know what the current weather is
 *           - content: "weather now"
 *           - content-obj: Weather enum object
 *       - INFORM_REF : Used to let base know the current time
 *          - inResponseTo: "time"
 *          - content: "time" int as a string
 *  Preformatives Used:
 *       - QUERY_REF : Used to ask the TimeKeeperAgent for the time
 *          - content: "time"
 *****************************************************************************/
public abstract class BaseAgent extends Agent{
    private GlobalValues _current_globals;
    protected IMessageHandler _time_message_handler;
    private HashMap<MessageTemplate, IMessageHandler> _msg_handlers;

    private MessageTemplate weatherMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchSender(new AID("WeatherMan")));
    private MessageTemplate timeMessage = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchSender(new AID("TimeKeeper", true)));

    @Override
    protected void setup() {
        super.setup();
        _current_time = getCurrentTimeBlocking();
        _time_message_handler = new HandleTimeMessage();
        _msg_handlers = new HashMap<MessageTemplate, IMessageHandler>();
        addMessageHandler(timeMessage, _time_message_handler);
        addMessageHandler(weatherMessageTemplate, new weatherMessageHandler());
        this.addMessageHandlingBehavior();
    }

    abstract protected void TimeExpired ();

    protected void addMessageHandler(MessageTemplate template, IMessageHandler handler) {
        _msg_handlers.put(template, handler);
    }

    // Used to tell someone that you don't understand there message.
    protected void sendNotUndersood(ACLMessage originalMsg, String content) {
        ACLMessage response = new ACLMessage();
        response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        response.setContent(content);
        response.setInReplyTo(originalMsg.getReplyWith());
        response.addReceiver(originalMsg.getSender());
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
                if (msg.getContent().equals("next time now")) {
                    _current_time ++;
                    TimeExpired();
                }
                else if (msg.getContent().equals("next time in")) {
                    try{
                        _time_expiring_in = (double)msg.getContentObject();
                    } catch (UnreadableException e) {}
                }
            }
        }
    }



    private class weatherMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            if (msg.getContent().equals("weather now")) {
                try {
                    _weather = (Weather) msg.getContentObject();
                } catch (UnreadableException e) {}
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
