package edu.swin.hets.agent;

import edu.swin.hets.agent.BaseAgent;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Vector;
/******************************************************************************
 *  Use: An agent for dealing with development messages.
 *  Name: Always only have one of these on the main container, have it name set
 *        to "LoggingAgent"
 *  Notes: There is a format to these messages, please use the base log methods
 *         to send a message rather than sending messages directly.
 *  Messages understood:
 *       - INFORM : Used to send messages about the system
 *          - content : Information about the system.
 *****************************************************************************/
public class LoggingAgent extends BaseAgent{
    private Vector<String> _logged_debug;
    private Vector<String> _logged_errors;
    private Vector<String> _logged_verbose;

    private MessageTemplate ErrorMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            GoodMessageTemplates.ContatinsString("error:"));
    private MessageTemplate VerboseMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            GoodMessageTemplates.ContatinsString("verbose:"));
    private MessageTemplate DebugMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            GoodMessageTemplates.ContatinsString("debug:"));

    @Override
    protected void setup() {
        System.out.println("Making logging");
        super.setup();
        _logged_debug = new Vector<String>();
        _logged_errors = new Vector<String>();
        _logged_verbose = new Vector<String>();
        addMessageHandler(ErrorMessageTemplate, new ErrorMessageHandler());
        addMessageHandler(VerboseMessageTemplate, new VerboseMessageHandler());
        addMessageHandler(DebugMessageTemplate, new DebugMessageHandler());
    }

    protected void TimeExpired() {
        // Dump an update to the file
        //TODO Add a file dump here.
        //TODO Append time to end of each message
    }

    protected void TimePush(int ms_left) {
    }

    protected String getJSON() {
        return "Not implemented";
    }

    private class ErrorMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            System.out.println(msg.getContent());
            _logged_errors.add(msg.getContent());
        }
    }

    private class VerboseMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            System.out.println(msg.getContent());
            _logged_verbose.add(msg.getContent());
        }
    }
    private class DebugMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            System.out.println(msg.getContent());
            _logged_debug.add(msg.getContent());
        }
    }
}
