package Agents.Other;

import Helpers.GlobalValues;
import Helpers.GoodMessageTemplates;
import Helpers.IMessageHandler;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
 *          - content-obj : Serialized global values object
 *   Messages sent:
 *       - NOT_UNDERSTOOD : Response from base if no one deals with a message
 *          - content : "no handlers found"
 *****************************************************************************/
public abstract class BaseAgent extends Agent{
    protected GlobalValues _current_globals;
    private HashMap<MessageTemplate, IMessageHandler> _msg_handlers;

    private MessageTemplate globalValuesChangedTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            GoodMessageTemplates.ContatinsString("GlobalValues"));

    @Override
    protected void setup() {
        super.setup();
        _msg_handlers = new HashMap<MessageTemplate, IMessageHandler>();
        addMessageHandler(globalValuesChangedTemplate, new GlobalsChangedHandler());
        this.addMessageHandlingBehavior();
        _current_globals = getCurrentGlobalValuesBlocking();
    }

    // Called to signal that the time has expired
    abstract protected void TimeExpired ();
    // Called to signal that we are now slightly further along in time.
    abstract protected void TimePush (int ms_left);

    protected void addMessageHandler(MessageTemplate template, IMessageHandler handler) {
        _msg_handlers.put(template, handler);
    }

    // Used to tell someone that you don't understand there message.
    protected void sendNotUndersood(ACLMessage originalMsg, String content) {
        //TODO, should log all uses of this into an error log file, just need to decide on a system.
        ACLMessage response = originalMsg.createReply();
        response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        response.setContent(content);
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
                        System.out.println("Test");
                        LogDebug("Message not understood: " + msg.getContent());
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
                    } else {
                        TimePush(_current_globals.getTimeLeft() * 1000);
                    }
                    _current_globals = newGlobals;
                }
            } catch (UnreadableException e) {
                sendNotUndersood(msg, "invalid globals attached");
            }
        }
    }

    protected void RegisterAMSService (String serviceName) {
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {

            //TODO Add logging and handle this better.
            e.printStackTrace();
        }
    }

    protected void DeRegisterService () {
        try  {
            DFService.deregister(this);
        } catch (Exception e) {
            //TODO Add logging and handle this better.
            e.printStackTrace();
        }
    }

    protected DFAgentDescription[] getService(String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( serviceName );
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            if (result.length == 0) {
                LogDebug("No " + serviceName + " services found");
            }
            return result;
        } catch (Exception e) {
            LogError("Could not contact the DF, error thrown");
        }
        return null;
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
            LogError("Problem searching AMS");
            e.printStackTrace();
        }
        return agents;
    }

    protected void LogError (String toLog) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("error:".concat(toLog));
        msg.addReceiver(new AID("LoggingAgent", true));
        send(msg);
    }

    protected void LogDebug (String toLog) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("debug:".concat(toLog));
        System.out.println("Logging: " + msg.getContent());
        msg.addReceiver(new AID("LoggingAgent", true));
        send(msg);
    }

    protected void LogVerbose (String toLog) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("verbose:".concat(toLog));
        msg.addReceiver(new AID("LoggingAgent", true));
        send(msg);
    }
}
