package edu.swin.hets.agent;

import edu.swin.hets.helper.GlobalValues;
import edu.swin.hets.helper.Weather;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Vector;

/******************************************************************************
 *  Use:  An agent to push out global values that change each cycle, for the
 *        moment that is just time and weather. We can extend latter
 *  Name: Always only have one of these IsOn the main container, have it name set
 *        to "GlobalValues"
 *  Preformatives Used:
 *       - INFORM : Used to send out all of the global variables for this
 *                  time slice.
 *          - content-obj : Serialized global values object
 *****************************************************************************/
public class GlobalValuesAgent extends Agent {
    public static final String AGENT_NAME = "GlobalValues";

    private GlobalValues _currentGlobalValue;
    private int _currentTime;
    private int _timeLeft;
    private double _lastAveragePrice;
    private Weather _currentWeather;
    private Vector<Weather> _predictions;

    protected void setup() {
        super.setup();
        addBehaviors();
        _lastAveragePrice = 1.6; //TODO, change to be the actual average.
        _currentTime = 0;
        _timeLeft = 5;
        _currentWeather = Weather.getRandom();
        _currentGlobalValue = new GlobalValues(_currentTime, _currentWeather, _timeLeft, _lastAveragePrice);
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
                    msg.setContentObject(_currentGlobalValue);
                } catch (IOException e) {
                    return; //TODO Add error logging here, once we agree IsOn a system.
                }
                send(msg);
            }
        });
    }

    // Call 5 times before we change the time.
    private void UpdateGlobalValues() {
        _timeLeft -= 1000;
        if (_timeLeft <= 0) {
            _currentTime++;
            _currentWeather = _predictions.elementAt(0);
            _predictions.removeElementAt(0);
            _predictions.add(Weather.getRandom());
            _timeLeft = 5000;
        }
        _currentGlobalValue = new GlobalValues(_currentTime, _currentWeather, _timeLeft, _lastAveragePrice);
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