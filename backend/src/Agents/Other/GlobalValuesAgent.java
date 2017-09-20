package Agents.Other;

import Helpers.GlobalValues;
import Helpers.Weather;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.util.Vector;
/******************************************************************************
 *  Use:  An agent to push out global values that change each cycle, for the
 *        moment that is just time and weather.
 *  Name: Always only have one of these on the main container, have it name set
 *        to "GlobalValues"
 *  Preformatives Used:
 *       - INFORM : Used to send out all of the global variables for this
 *                  time slice.
 *          - content : "new globals"
 *          - content-obj : Serialized global values object
 *       - INFORM-REF : Used to send out global variables in response to a
 *                      question.
 *          - content : "new globals"
 *          - content-obj : Serialized global values object
 *       - INFORM : Used to send out new globals and signal the next time-slice
 *          - content : "new time-slice"
 *          - content-obj : Serialized global values object
 *  Preformatives Understood:
 *       - QUERY-REF : Used to ask for the current global values
 *          - content : "new globals"
 *          - content-obj : Serialized global values object
 *****************************************************************************/
public class GlobalValuesAgent extends Agent {
    private GlobalValues _current_global_value;
    private int _current_time;
    private Weather _current_weather;
    private Vector<Weather> _predictions;
    private MessageTemplate getValuesTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF),
            MessageTemplate.MatchContent("new globals"));

    protected void setup() {
        super.setup();
        addBehaviors();
        _current_time = 0;
        _current_weather = Weather.getRandom();
        _current_global_value = new GlobalValues(_current_time, _current_weather);
        _predictions = new Vector<Weather>();
        for (int i = 0; i < 10; i++) {
            _predictions.add(Weather.getRandom()); // Make forecast random for the moment.
        }
    }

    private void addBehaviors() {
        // Deal with any requests for the time.
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            public void onTick() {
                UpdateGlobalValues();
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("new globals");
                AMSAgentDescription[] agents = getAgentList();
                for (AMSAgentDescription agent: agents) {
                    if (agent.getName() != this.getAgent().getAID()) {
                        msg.addReceiver(agent.getName());
                    }
                }
                try{
                    msg.setContentObject(_current_global_value);
                } catch (IOException e) {}
                send(msg);
            }
        });
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive(getValuesTemplate);
                if (msg != null) {
                    // We got a request for current values
                    GetGlobalsHandler(msg);
                }
                block();
            }
        });
    }

    private void UpdateGlobalValues() {
        _current_time++;
        _current_weather = _predictions.elementAt(0);
        _predictions.removeElementAt(0);
        _predictions.add(Weather.getRandom());
        _current_global_value = new GlobalValues(_current_time, _current_weather);
    }

    public void GetGlobalsHandler(ACLMessage msg) {
        ACLMessage response = new ACLMessage(ACLMessage.INFORM_REF);
        response.setInReplyTo(msg.getReplyWith());
        response.setContent("new globals");
        response.addReceiver(msg.getSender());
        try{
            msg.setContentObject(_current_global_value);
        } catch (IOException e) {
            return;
        }
        send(response);
    }

    // Method to return list of agents in the platform (taken from AMSDumpAgent from Week3)
    private AMSAgentDescription[] getAgentList() {
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
