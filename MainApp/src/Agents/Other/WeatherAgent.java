package Agents.Other;

import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;
import Helpers.Weather;
/******************************************************************************
 *  Use: Used by other agents to find the current weather conditions and get
 *       predictions about future weather events.
 *  Preformatives understood:
 *       - query-ref : Used to ask about the weather
 *             syntax: "weather"
 *       - query-ref : Used to ask what the weather will be like at some time
 *             syntax: "weather at:xxx" Where xxx is int as a string
 *   Preformatives Used:
 *       - QUERY_REF : Used to ask the TimeKeeperAgent for the time
 *          - content: "time"
 *       - INFORM : Used to let the agent know what the current weather is
 *           - content: "weather now"
 *           - content-obj: Weather enum object
 *****************************************************************************/
public class WeatherAgent extends BaseAgent{
    private Weather _currentWeather;
    private Vector<Weather> _predictions;

    @Override
    protected void setup() {
        super.setup();
        _currentWeather = Weather.getRandom();
        _predictions = new Vector<Weather>();
        for (int i = 0; i < 10; i++) {
            _predictions.add(Weather.getRandom()); // Make forecast random for the moment.
        }
    }

    // Make current weather Remove latest prediction and make new one.
    // Also send an update to everyone about the weather.
    protected void TimeExpired (){
        _currentWeather = _predictions.elementAt(0);
        _predictions.removeElementAt(0);
        _predictions.add(Weather.getRandom());
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        AMSAgentDescription[] agents = getAgentList();
        for (AMSAgentDescription agent : agents) {
            message.addReceiver(agent.getName());
        }
        message.setContent("weather now");
        try {
            message.setContentObject(_currentWeather);
        } catch (IOException e) {}
    }

    protected void UnhandledMessage(ACLMessage msg) {
        if (msg.getPerformative() == ACLMessage.QUERY_REF) {
            if (msg.getContent().equals("weather")) {
                    ACLMessage response = new ACLMessage();
                    response.setInReplyTo(msg.getReplyWith());
                    response.setPerformative(ACLMessage.INFORM_REF);
                    response.addReceiver(msg.getSender());
                    response.setContent(_currentWeather.toString());
                    send(response);
            }
            else if (msg.getContent().contains("weather at:")) {
                    // For the moment just return the current weather, it never changes.
                    ACLMessage response = new ACLMessage();
                    response.setInReplyTo(msg.getReplyWith());
                    response.setPerformative(ACLMessage.INFORM_REF);
                    response.addReceiver(msg.getSender());
                    response.setContent(_currentWeather.toString());
                    send(response);
            }
         }
        else { //Not a query message, send back a not understood.
            sendNotUndersood(msg, "Unused preformative");
        }
    }
}
