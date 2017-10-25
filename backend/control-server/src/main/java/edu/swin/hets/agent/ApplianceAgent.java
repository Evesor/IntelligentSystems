package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.agentConfigurationOntology.AddBehaviour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplianceAgent extends BaseAgent
{
	private static final int GROUP_ID = 4;
	private static int DEFAULT_WATT_VALUE;
	boolean on;
	private String _simpleHomeName;
	private TickerBehaviour findHomeBehavior = new TickerBehaviour(this, 100) {
		@Override
		protected void onTick() {
			findHome();
		}
	};
	//TODO current array
	//should be vector
	private ArrayList<Integer> historyOfCurrentUsage;
	//int[] historyOfCurrentUsage = new int[48];
	//TODO forecast array
	//should be vector, should store enumeration instead of int
	//private ArrayList<Integer> forecast;
	//int[] forecast = new int[48];
	int watt;

	//askHomeIfWeCanTurnOn_Off on this appliance
	//ACLMessage.REQUEST, "on"
	private MessageTemplate R_On = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("on"));

	//askHomeIfWeCanTurnOn_Off off this appliance
	//ACLMessage.REQUEST, "off"
	private MessageTemplate R_Off = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("off"));

	//electricity request accepted || declined
	//ACLMessage.INFORM, "electricity,1" || "electricity,0"
	private MessageTemplate I_Electricity = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity"));

	//initialize variables
	private void init()
	{
		on = true;
		historyOfCurrentUsage = new ArrayList<>();
		historyOfCurrentUsage.add(0);
		historyOfCurrentUsage.add(0);
//		historyOfCurrentUsage.add(0);
//		historyOfCurrentUsage.add(0);
		//forecast = new ArrayList<>();
//		forecast.add(0);
//		forecast.add(0);
//		forecast.add(0);

//		int i;
//		for(i=0;i<48;i++)
//		{
//			historyOfCurrentUsage[i] = 0;
//			forecast[i] = 0;
//		}
		Object[] args = getArguments();
		List<String> argument = (List<String>) args[0];
		if (argument.size() == 0) {
			LogError("was not passed a value to use for power consumption, using default");
			watt = DEFAULT_WATT_VALUE;
		} else {
			try	{ watt = Integer.parseInt(argument.get(0)); }
			catch (NumberFormatException e ) {
				LogError("Was passed a value that is not a valid int for initialization");
				watt = DEFAULT_WATT_VALUE;
			}
			_simpleHomeName = argument.get(1);
		}
		addBehaviour(findHomeBehavior);
		//updateForecastUsage();
		on = true;
	}

	@Override
	protected void setup()
	{
		super.setup();
		init();
		addMessageHandler(R_On, new ApplianceAgent.OnHandler());
		addMessageHandler(R_Off, new ApplianceAgent.OffHandler());
		addMessageHandler(I_Electricity, new ApplianceAgent.ElectricityHandler());
		sendCurrentUsage();
		sendForecastUsage();
	}

	private void findHome() {
		DFAgentDescription[] agents = getService(_simpleHomeName);
		if (agents.length > 1) LogError("Warning multiple agents registered as: " + _simpleHomeName);
		else if (agents.length == 1) sendIAmYoursToHome(agents[0].getName());
	}

	private void sendIAmYoursToHome(AID homeAID) {
		ACLMessage iAmYours = new ACLMessage(ACLMessage.INFORM);
		iAmYours.setContent("ApplianceDetail," + getName());
		iAmYours.addReceiver(homeAID);
		send(iAmYours);
		LogDebug("sending message to " + homeAID.getName());
		removeBehaviour(findHomeBehavior);
	}

	private class OnHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg){
			askHomeIfWeCanTurnOn_Off(true);}
	}

	private class OffHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg){
			askHomeIfWeCanTurnOn_Off(false);}
	}

	private class ElectricityHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			if(value==0){LogDebug(getLocalName() + " electricity request declined");}
			else if(value==1)
			{
				LogVerbose(getLocalName() + " electricity request approved. " + getLocalName() + " is now on");
				on = true;
			}
		}
	}

	private void sendCurrentUsage()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		//msg.setContent("electricity historyOfCurrentUsage," + historyOfCurrentUsage[_current_globals.getTime()]);
		msg.setContent("electricity historyOfCurrentUsage," + historyOfCurrentUsage.get(_current_globals.getTime()));
		msg.addReceiver(new AID("home1", AID.ISLOCALNAME));
		send(msg);
	}

	private void sendForecastUsage()
	{
		//updateForecastUsage();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("electricity forecast," + makeNewForecast());
		msg.addReceiver(new AID("home1", AID.ISLOCALNAME));
		send(msg);
	}

	private double makeNewForecast() {
		//TODO, Make average of usage
		return watt*5;
	}

	//TODO updateForcastUsage function
	//calculate forecast usage and update variable
//	private void updateForecastUsage()
//	{
//		//forecast[_current_globals.getTime()] = watt*5;
//		forecast.set(_current_globals.getTime(),watt*5);
//	}

	private void sendElectricityRequest()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("electricity," + watt);
		msg.addReceiver(new AID("home1",AID.ISLOCALNAME));
		send(msg);
	}

	private void askHomeIfWeCanTurnOn_Off(boolean on)
	{
		//compare with historyOfCurrentUsage state
		if(this.on!=on)
		{
			if(on==true)
			{
				//send electricity request to home agent
				//home agent check historyOfCurrentUsage usage with max usage
				//if historyOfCurrentUsage + request < max usage, approve
				sendElectricityRequest();

				LogDebug(getLocalName() + " sent an electricity request");
			}
			else if(on==false)
			{
				this.on = false;
				LogVerbose(getLocalName() + " is now off");
			}
		}
	}

	@Override
	protected void TimeExpired()
	{
		//count electricity usage
		if(on == true)
		{
			//historyOfCurrentUsage.add(new Integer[4]);
			//current.set(_current_globals.getTime(),historyOfCurrentUsage.get(_current_globals.getTime())+watt);
			//historyOfCurrentUsage[_current_globals.getTime()] += watt;
			historyOfCurrentUsage.add(watt);
			LogDebug("historyOfCurrentUsage : " + historyOfCurrentUsage.get(_current_globals.getTime()));
			sendForecastUsage();
		}
		historyOfCurrentUsage.add(0);
	}

	@Override
	protected void TimePush(int ms_left)
	{
		//count electricity usage
		if(on == true)
		{
			historyOfCurrentUsage.set(_current_globals.getTime(), historyOfCurrentUsage.get(_current_globals.getTime())+watt);
			//historyOfCurrentUsage[_current_globals.getTime()] += watt;
			//historyOfCurrentUsage.get(_current_globals.getTime())[5 - (ms_left/ GlobalValues.pushTimeLength())] =
			//historyOfCurrentUsage.set(_current_globals.getTime(),historyOfCurrentUsage.get(_current_globals.getTime())+watt);
			LogDebug("historyOfCurrentUsage : " + historyOfCurrentUsage.get(_current_globals.getTime()));
		}
	}

	//TODO Override getJSON
	@Override
	protected String getJSON(){
		String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(
					new ApplianceAgentData());
		}
		catch (JsonProcessingException e) {
			LogError("Error parsing data to json in " + getName() + " exeption thrown");
		}
		return json;
	}
	/******************************************************************************
	 *  Use: Used by JSON serializing library to make JSON objects.
	 *****************************************************************************/
	private class ApplianceAgentData implements Serializable{
		private AgentData data;
		ApplianceAgentData () {
			data = new AgentData();
		}
		public int getgroup() { return GROUP_ID; }
		public AgentData getagent() {return data; }
		public String getid() {return getName();}
		private class AgentData {
			public String getName () { return getLocalName();}
			public Integer getWattage () {return watt;}
			public Boolean getOn() {return on; }
			public ArrayList<Integer> getCurrent() { return historyOfCurrentUsage; }
			//public ArrayList<Integer> getForecast() { return forecast; }
		}
	}
}
//TODO other list
//appliance send electricity request / home approve before turning it on?
//1 day history for usage and forecast, time index??
//send electricity usage for each time push / home calculate electricity usage?
//Forecast