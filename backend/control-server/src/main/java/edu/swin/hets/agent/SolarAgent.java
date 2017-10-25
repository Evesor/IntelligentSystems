package edu.swin.hets.agent;

import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SolarAgent extends BaseAgent {

    private MessageTemplate I_PowerInform = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            GoodMessageTemplates.ContatinsString("electricity"));

    @Override
    protected void setup() {
        super.setup();
    }

    // Update bookkeeping.
    protected void TimeExpired (){ }

    protected String getJSON() {
        String json = "";
        return json;
    }

    protected void TimePush(int ms_left) { }

    // Someone agreeing to buy electricity from us.
    private class PowerInformRequestHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            ACLMessage response = msg.createReply();
            int toReturn = 0;

            switch(_current_globals.getWeather())
            {
                case Night:     toReturn = 0;   break;
                case Sunny:     toReturn = -5;  break;
                case VerySunny: toReturn = -10; break;
                case Overcast:  toReturn = -3;  break;
                default:        toReturn = 0;   break;
            }
            response.setContent(Integer.toString(toReturn));
        }
    }

    public int WeatherPredictionModel(int timeInAdvance)
    {
        return 0;
    }
}
