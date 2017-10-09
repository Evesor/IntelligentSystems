package edu.swin.hets.agent;

import edu.swin.hets.helper.GlobalValues;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import edu.swin.hets.web.NoOpWebSocketHandler;
import edu.swin.hets.web.WebSocketHandler;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

/******************************************************************************
 *  Use: Used to dump data periodically to file for the WS
 *  Name: Always only have one of these on the main container, have it name set
 *        to "WebServer"
 *  Preformatives used:
 *       - INFORM : Used to send info to server
 *             content: "any info that the server needs, as JSON"
 *****************************************************************************/
public class WebAgent extends BaseAgent {
    private static final Logger logger = LoggerFactory.getLogger(WebAgent.class);
    private Vector<String> messages;

    private MessageTemplate InformMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.not(GoodMessageTemplates.ContatinsString(GlobalValues.class.getName())));
    private WebSocketHandler clientWebSocketHandler = new NoOpWebSocketHandler();

    protected void setup() {
        super.setup();
        messages = new Vector<>();
        addMessageHandler(InformMessageTemplate, new InformMessageHandler());

        try {
            clientWebSocketHandler = (WebSocketHandler) getArguments()[0];
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            logger.error(e.toString());
            logger.warn("Websocket handler not found, check your agent init arguments");
            logger.warn("You may not be starting up JADE correctly");
            logger.warn("Defaulting to NO-OP implementation");
        }
    }

    protected void TimeExpired() {
        clientWebSocketHandler.broadcast("wow");
    }

    protected String getJSON() {
        return "Not implemented";
    }

    protected void TimePush(int ms_left) {     }
    /**
     * Incoming message handling implementation here
     */
    private class InformMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            LogVerbose("Web agent just got: " + msg.getContent());
            clientWebSocketHandler.broadcast(msg.getContent());
        }
    }
}