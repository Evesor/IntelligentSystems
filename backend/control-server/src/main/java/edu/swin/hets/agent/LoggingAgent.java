package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/******************************************************************************
 *  Use: An agent for dealing with development messages.
 *  Name: Always only have one of these IsOn the main container, have it name set
 *        to "LoggingAgent"
 *  Notes: There is a format to these messages, please use the base log methods
 *         to send a message rather than sending messages directly.
 *  Messages understood:
 *       - INFORM : Used to send messages about the system
 *          - content : Information about the system.
 *****************************************************************************/
public class LoggingAgent extends BaseAgent{
    public static final String AGENT_NAME = "LoggingAgent";

    private ArrayList<LoggedData> _logged_debug;
    private ArrayList<LoggedData> _logged_errors;
    private ArrayList<LoggedData> _logged_verbose;

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
        super.setup();
        _logged_debug = new ArrayList<>();
        _logged_errors = new ArrayList<>();
        _logged_verbose = new ArrayList<>();
        addMessageHandler(ErrorMessageTemplate, new ErrorMessageHandler());
        addMessageHandler(VerboseMessageTemplate, new VerboseMessageHandler());
        addMessageHandler(DebugMessageTemplate, new DebugMessageHandler());
    }

    protected void TimeExpired() {
        // Dump an update to the file
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("verbose: Time slice : " + _current_globals.getTime() + " is starting.");
        System.out.println("--------------------------------------------------------------------------------");
        //TODO Add a file dump here.
        //TODO Append time to end of each message
    }

    protected void TimePush(int ms_left) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("verbose: Time slice : " + _current_globals.getTime() + " has " + _current_globals.getTimeLeft() + " ms left");
        System.out.println("--------------------------------------------------------------------------------");
    }

    protected String getJSON() {
        String json = "test";
        try {
            json = new ObjectMapper().writeValueAsString(
                    new LoggingAgentData());
        }
        catch (JsonProcessingException e) {
            LogError("Error parsing data to json in " + getName() + " exeption thrown");
        }
        return json;
    }

    private class ErrorMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            System.out.println(msg.getContent() + ":: from:" + msg.getSender().getName());
            _logged_errors.add(new LoggedData(msg.getContent(),
                    _current_globals.getTime(),
                    _current_globals.getTimeLeft(),
                    msg.getSender().getName()));
        }
    }

    private class VerboseMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            System.out.println(msg.getContent() + ":: from:" + msg.getSender().getName());
            _logged_verbose.add(new LoggedData(msg.getContent(),
                    _current_globals.getTime(),
                    _current_globals.getTimeLeft(),
                    msg.getSender().getName()));
        }
    }
    private class DebugMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            System.out.println(msg.getContent() + ":: from:" + msg.getSender().getName());
            _logged_debug.add(new LoggedData(msg.getContent(),
                    _current_globals.getTime(),
                    _current_globals.getTimeLeft(),
                    msg.getSender().getName()));
        }
    }

    private class LoggingAgentData implements Serializable{
        public List<LoggedData> getVerboseLogs () { return _logged_verbose; }
        public List<LoggedData> getDebugLogs() { return  _logged_debug; }
        public List<LoggedData> getErrorLogs() { return _logged_errors; }
    }
    /******************************************************************************
     *  Use: Wrapper around messages to make sure we can check when the came in.
     *****************************************************************************/
    private class LoggedData implements Serializable {
        private String _message;
        private String _from;
        private Integer _timeSlice;
        private Integer _timeLeft;

        LoggedData (String message, Integer timeSlice, Integer timeLeft, String from) {
            _message = message;
            _timeSlice = timeSlice;
            _timeLeft = timeLeft;
            _from = from;
        }

        public String getLog() { return _message; }
        public Integer getTimeSlice () { return _timeSlice; }
        public Integer getTimeLeft() { return _timeLeft; }
        public String getFrom() { return  _from; }
    }
}
