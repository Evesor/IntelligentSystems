package edu.swin.hets.agent;

import com.hierynomus.msdtyp.ACL;
import edu.swin.hets.helper.*;
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

import java.io.IOException;
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
 *       - NOT_UNDERSTOOD : logs a not understood message
 *          - content : "error message"
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
    private MessageTemplate messageNotUndersoodTemplate = MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);

    @Override
    protected void setup() {
        super.setup();
        _msg_handlers = new HashMap<MessageTemplate, IMessageHandler>();
        addMessageHandler(globalValuesChangedTemplate, new GlobalsChangedHandler());
        addMessageHandler(messageNotUndersoodTemplate, new MessageNotUnderstoodHandler());
        this.addMessageHandlingBehavior();
        _current_globals = getCurrentGlobalValuesBlocking();
    }

    // Called to signal that the time has expired
    abstract protected void TimeExpired ();
    // Called to signal that we are now slightly further along in time.
    abstract protected void TimePush (int ms_left);
    // Called to get the internal data of this agent to push to the web server
    abstract protected String getJSON ();

    protected void addMessageHandler(MessageTemplate template, IMessageHandler handler) {
        _msg_handlers.put(template, handler);
    }

    // Used to tell someone that you don't understand there message.
    protected void sendNotUndersood(ACLMessage originalMsg, String content) {
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
                        sendNotUndersood(msg, "no handlers found for " + msg.getPerformative());
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
                        //SendAgentDetailsToServer(getJSON()); //TODO Uncomment when we get the WS stuff up and running.
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

    // Used to send the server the object details as a JSON string
    private void SendAgentDetailsToServer (String detailsAsJSON) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("WebServer", AID.ISLOCALNAME));
        msg.setContent(detailsAsJSON);
        send(msg);
    }

    private class MessageNotUnderstoodHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            LogDebug("message not undersood reciver: " + getName() +
                    " sender: " + msg.getSender().getName() + " content: " + msg.getContent());
        }
    }

    protected void RegisterAMSService (String serviceName,String serviceType) {
        LogVerbose("registering a " + serviceType + " service from " + serviceName);
        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType(serviceType);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            LogError("Could not add a " + serviceType + " service, exception thrown: " + e.getMessage());
        }
    }

    protected void DeRegisterService () {
        try  {
            DFService.deregister(this);
        } catch (Exception e) {
            LogError("Could not de register a service, exception thrown");
        }
    }

    protected DFAgentDescription[] getService(String serviceType) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            if (result.length == 0) {
                LogDebug("No " + serviceType + " services found");
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

    protected void addPowerSaleAgreement(ACLMessage msg, PowerSaleAgreement ag) {
        try {
            msg.setContentObject(ag);
        }catch (IOException e) {
            LogError("Tried to attach a power sale agreement to message, error thrown");
            return;
        }
    }

    protected void addPowerSaleProposal(ACLMessage msg, PowerSaleProposal prop) {
        try {
            msg.setContentObject(prop);
        }catch (IOException e) {
            LogError("Tried to attach a power sale agreement to message, error thrown");
            return;
        }
    }

    protected PowerSaleProposal getPowerSalePorposal(ACLMessage msg) {
        try {
            return (PowerSaleProposal)msg.getContentObject();
        }catch (UnreadableException e) {
            LogError("Tried to read a power sale agreement from message, error thrown");
            return null;
        }
    }

    protected PowerSaleAgreement getPowerSaleAgrement (ACLMessage msg) {
        try {
            return (PowerSaleAgreement) msg.getContentObject();
        }catch (UnreadableException e) {
            LogError("Tried to read a power sale agreement from message, error thrown");
            return null;
        }
    }

    protected void LogError (String toLog) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("error: ".concat(toLog));
        msg.addReceiver(new AID("LoggingAgent", AID.ISLOCALNAME));
        send(msg);
    }

    protected void LogDebug (String toLog) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("debug: ".concat(toLog));
        msg.addReceiver( new AID("LoggingAgent", AID.ISLOCALNAME));
        send(msg);
    }

    protected void LogVerbose (String toLog) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("verbose: ".concat(toLog));
        msg.addReceiver(new AID("LoggingAgent", AID.ISLOCALNAME));
        send(msg);
    }
}