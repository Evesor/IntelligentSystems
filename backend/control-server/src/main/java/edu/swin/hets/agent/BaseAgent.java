package edu.swin.hets.agent;

import edu.swin.hets.helper.GlobalValues;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
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
/******************************************************************************
 *  Use: An abstract base agent class used to provide all of the default global
 *       value management and checking we need.
 *  Notes:
 *       - Message handling: Base agent deals with all message que interactions
 *         for an agent. To respond to a message from a sub class of BaseAgent
 *         invoke the register message command with the message template and
 *         the function to handle the message.
 *  Messages understood:
 *       - INFORM : Used to send out all of the global variables for this
 *                  time slice.
 *          - content : "new globals"
 *          - content-obj : Serialized global values object
 *       - INFORM : Used to send out new globals and signal the next time-slice
 *          - content : "new time-slice"
 *          - content-obj : Serialized global values object
 *        - INFORM-REF : Used to get back global values
 *          - content : "new globals"
 *          - content-obj : Serialized global values object
 *   Messages sent:
 *       - NOT_UNDERSTOOD : Response from base if no one deals with a message
 *          - content : "no handlers found"
 *       - INFORM : D
 *          - content : "agent data"
 *          - content-obj : State data as JSON, string object
 *****************************************************************************/
public abstract class BaseAgent extends Agent{
    protected GlobalValues _current_globals;
    private HashMap<MessageTemplate, IMessageHandler> _msg_handlers;
    // Templates used
    private MessageTemplate globalValuesChangedTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            GoodMessageTemplates.ContatinsString("GlobalValues"));
    private MessageTemplate globalValuesTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF),
            GoodMessageTemplates.ContatinsString("GlobalValues"));

    @Override
    protected void setup() {
        super.setup();
        _msg_handlers = new HashMap<MessageTemplate, IMessageHandler>();
        addMessageHandler(globalValuesChangedTemplate, new GlobalsChangedHandler());

        //_current_globals = getCurrentGlobalValuesBlocking();
        this.addMessageHandlingBehavior();
    }

    abstract protected void TimeExpired ();

    protected void addMessageHandler(MessageTemplate template, IMessageHandler handler) {
        _msg_handlers.put(template, handler);
    }

    // Used to tell someone that you don't understand there message.
    protected void sendNotUndersood(ACLMessage originalMsg, String content) {
        ACLMessage response = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
        response.setContent(content);
        response.setInReplyTo(originalMsg.getReplyWith());
        response.addReceiver(originalMsg.getSender());
        send(response);
    }

    // Used by the agent at construction to make sure that it get a time at initialization.
    private GlobalValues getCurrentGlobalValuesBlocking() {
            ACLMessage msg = blockingReceive(globalValuesChangedTemplate);
                // We got the correct message, try and grab the object
                try {
                    return (GlobalValues) msg.getContentObject();
                } catch (UnreadableException e) {
                    return null;
                }

    }

    // Add the message handling to the base class.
    private void addMessageHandlingBehavior () {
        // Deal with any messages to this agent by adding them to the messages vector
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    boolean message_handled = false;
                    Iterator keys = _msg_handlers.keySet().iterator();
                    while(keys.hasNext()) {
                        MessageTemplate template = (MessageTemplate) keys.next();
                        if (template.match(msg)) {
                            // We found a message, pass it to its handler function.
                            _msg_handlers.get(template).Handler(msg);
                            message_handled = true;
                        }
                    }
                    if (!message_handled) {
                        System.out.println("Not undersood" + msg.getContent());
                        sendNotUndersood(msg, "no handlers found");
                    }
                }
                block();
            }
        });
    }

    private class GlobalsChangedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            try {
                GlobalValues newGlobals = (GlobalValues) msg.getContentObject();
                if (_current_globals != null) {
                    if (newGlobals.getTime() != _current_globals.getTime()) {
                        TimeExpired();
                    }
                    _current_globals = newGlobals;
                }
            } catch (UnreadableException e) {
                sendNotUndersood(msg, "invalid globals attached");
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
