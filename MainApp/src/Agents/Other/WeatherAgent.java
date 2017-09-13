package Agents.Other;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Vector;
/******************************************************************************
 *  Enums for types of weather we can have in our system
 *****************************************************************************/
enum Weather {
    VerySunny ,Sunny, Overcast, Night;

    public static Weather getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
}
/******************************************************************************
 *  Use: Used by other agents to find the current weather conditions and get
 *       predictions about future weather events.
 *  Preformatives used:
 *       - query-ref : Used to ask about the weather
 *             syntax: "weather"
 *       - query-ref : Used to ask what the weather will be like at some time
 *             syntax: "weather at:xxx" Where xxx is int as a string
 *****************************************************************************/
public class WeatherAgent extends BaseAgent{
    private Weather _currentWeather;
    private Vector<Weather> _predicitons;

    @Override
    protected void setup() {
        super.setup();
        // Needs to be random.
        _currentWeather = Weather.getRandom();
        _predicitons = new Vector<Weather>();
        for (int i = 0; i < 10; i++) {
            _predicitons.add(Weather.getRandom()); // Make forecast random for the moment.
        }
    }

    // Make current weather Remove latest prediction and make new one
    protected void TimeExpired (){
        _currentWeather = _predicitons.elementAt(0);
        _predicitons.removeElementAt(0);
        _predicitons.add(Weather.getRandom());
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

    protected void SaleMade(ACLMessage msg) {
        // Weathermen don't care about sales.
    }

    protected void TimeExpiringIn(int expireTimeMS){
        // Dose not care about when time will expire
    }
}
