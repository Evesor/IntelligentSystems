package Agents.Other;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
/******************************************************************************
 *  Enums for types of weather we can have in our system
 *****************************************************************************/
enum Weather {
    VerySunny ,Sunny, Overcast, Night
}
/******************************************************************************
 *  Use: Used by other agents to find the current weather conditions and get
 *       predictions about future weather events.
 *  Preformatives used:
 *       - query-ref : Used to ask about the weather
 *             syntax: "weather"
 *       - query-ref : Used to ask what the weather will be like at some time
 *             syntax: "weather-at"
 *             data: Time object
 *****************************************************************************/
public class WeatherAgent extends Agent{
    private Weather _currentWeather;

    @Override
    protected void setup() {
        super.setup();
        // Needs to be random.
        _currentWeather = Weather.Overcast;
        this.addResponseBehavior();
    }
    // Add all of our behaviors, only call once at construction.
    private void addResponseBehavior () {
        // Deal with any messages to this agent by adding them to the messages vector
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.QUERY_REF) {
                        switch (msg.getContent()){
                            case "weather": {
                                ACLMessage response = new ACLMessage();
                                response.setInReplyTo(msg.getReplyWith());
                                response.setPerformative(ACLMessage.INFORM_REF);
                                response.addReceiver(msg.getSender());
                                response.setContent(_currentWeather.toString());
                                send(response);
                                break;
                            }
                            case "weather-at": {
                                // For the moment just return the current weather, it never changes.
                                ACLMessage response = new ACLMessage();
                                response.setInReplyTo(msg.getReplyWith());
                                response.setPerformative(ACLMessage.INFORM_REF);
                                response.addReceiver(msg.getSender());
                                response.setContent(_currentWeather.toString());
                                send(response);
                                break;
                            }
                            default: {
                                sendNotUndersood(msg, "Not valid content");
                            }
                        }
                    }
                    else { //Not a query message, send back a not understood.
                        sendNotUndersood(msg, "Unused preformative");
                    }
                }
                block();
            }
        });
    }
    // Used to tell someone that you don't understand there message.
    private void sendNotUndersood(ACLMessage origionalMsg, String content) {
        ACLMessage response = new ACLMessage();
        response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        response.setContent(content);
        response.setInReplyTo(origionalMsg.getReplyWith());
        response.addReceiver(origionalMsg.getSender());
        send(response);
    }
}
