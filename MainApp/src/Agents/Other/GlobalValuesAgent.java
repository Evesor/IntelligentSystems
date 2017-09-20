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
 *        moment that is just time and weather. We can extend latter
 *  Name: Always only have one of these on the main container, have it name set
 *        to "GlobalValues"
 *  Preformatives Used:
 *       - INFORM : Used to send out all of the global variables for this
 *                  time slice.
 *          - content-obj : Serialized global values object
 *****************************************************************************/
public class GlobalValuesAgent extends Agent {
    private GlobalValues _current_global_value;
    private int _current_time;
    private int _time_left;
    private Weather _current_weather;
    private Vector<Weather> _predictions;

    protected void setup() {
        super.setup();
        addBehaviors();
        _current_time = 0;
        _time_left = 5;
        _current_weather = Weather.getRandom();
        _current_global_value = new GlobalValues(_current_time, _current_weather, _time_left);
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
                } catch (IOException e) {
                    return; //TODO Add error logging here, once we agree on a system.
                }
                send(msg);
            }
        });
    }

    // Call 5 times before we change the time.
    private void UpdateGlobalValues() {
        _time_left--;
        if (_time_left <= 0) {
            _current_time++;
            _current_weather = _predictions.elementAt(0);
            _predictions.removeElementAt(0);
            _predictions.add(Weather.getRandom());
            _time_left = 5;
        }
        _current_global_value = new GlobalValues(_current_time, _current_weather, _time_left);
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
