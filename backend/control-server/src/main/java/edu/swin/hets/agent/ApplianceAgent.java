package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplianceAgent extends BaseAgent
{
	private static final int GROUP_ID = 4;
	private static int DEFAULT_WATT_VALUE = 15;
	private boolean _isOn;
	private int _wattUsage;
	private String _simpleHomeName;
	@Nullable
	private String _actualHomeName;
	private ArrayList<Integer> _historyOfCurrentUsage;
	private TickerBehaviour _findHomeBehavior = new TickerBehaviour(this, 100) {
		@Override
		protected void onTick() { findHome(); }
	};

	private MessageTemplate RequestPowerOnMessageTemplate = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("_isOn"));

	private MessageTemplate RequestPowerOffMessageTemplate = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("off"));

	private MessageTemplate ElectricityRequestResponseHandler = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity"));

	//initialize variables
	private void init()
	{
		_actualHomeName = null;
		_historyOfCurrentUsage = new ArrayList<>();
		Object[] args = getArguments();
		List<String> argument = (List<String>) args[0];
		if (argument.size() != 2) {
			LogError("was not passed a value to use for power consumption, and home using default");
			_wattUsage = DEFAULT_WATT_VALUE;
		} else {
			try	{ _wattUsage = Integer.parseInt(argument.get(0)); }
			catch (NumberFormatException e ) {
				LogError("Was passed a value that is not a valid int for initialization");
				_wattUsage = DEFAULT_WATT_VALUE;
			}
			_simpleHomeName = argument.get(1);
		}
		addBehaviour(_findHomeBehavior);
		_isOn = true;
	}

	@Override
	protected void setup()
	{
		super.setup();
		init();
		addMessageHandler(RequestPowerOnMessageTemplate, new ApplianceAgent.OnHandler());
		addMessageHandler(RequestPowerOffMessageTemplate, new ApplianceAgent.OffHandler());
		addMessageHandler(ElectricityRequestResponseHandler, new ApplianceAgent.ElectricityHandler());
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
		_actualHomeName = homeAID.getName();
		send(iAmYours);
		LogDebug("sending message to " + homeAID.getName());
		removeBehaviour(_findHomeBehavior);
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
				LogVerbose(getLocalName() + " electricity request approved. " + getLocalName() + " is now _isOn");
				_isOn = true;
			}
		}
	}

	private void sendCurrentUsage()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		//msg.setContent("electricity _historyOfCurrentUsage," + _historyOfCurrentUsage[_current_globals.getTime()]);
		//msg.setContent("electricity _historyOfCurrentUsage," + _historyOfCurrentUsage.get(_current_globals.getTime()));
		sendHomeMessage(msg);
	}

	private void sendForecastUsage()
	{
		//updateForecastUsage();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("electricity forecast," + makeNewForecast());
		sendHomeMessage(msg);
	}

	private double makeNewForecast() {
		//TODO, Make average of usage
		LogDebug("OSSU" + _wattUsage);
		return _wattUsage *5;
	}

	private void sendElectricityRequest()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("electricity," + _wattUsage);
		sendHomeMessage(msg);
	}

	private void sendHomeMessage(ACLMessage msg) {
		if(_actualHomeName != null) {
			msg.addReceiver(new AID(_actualHomeName, AID.ISLOCALNAME));
		}
	}

	private void askHomeIfWeCanTurnOn_Off(boolean on)
	{
		//compare with _historyOfCurrentUsage state
		if(_isOn != on)
		{
			if(on==true)
			{
				//send electricity request to home agent
				//home agent check _historyOfCurrentUsage usage with max usage
				//if _historyOfCurrentUsage + request < max usage, approve
				sendElectricityRequest();

				LogDebug(getLocalName() + " sent an electricity request");
			}
			else if(on==false)
			{
				this._isOn = false;
				LogVerbose(getLocalName() + " is now off");
			}
		}
	}

	@Override
	protected void TimeExpired()
	{
		_historyOfCurrentUsage.add((_isOn ? _wattUsage : 0));
	}

	@Override
	protected void TimePush(int ms_left)
	{
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
			public Integer getWattage () {return _wattUsage;}
			public Boolean getOn() {return _isOn; }
			public ArrayList<Integer> getCurrent() { return _historyOfCurrentUsage; }
			//public ArrayList<Integer> getForecast() { return forecast; }
		}
	}
}
//TODO other list
//appliance send electricity request / home approve before turning it _isOn?
//1 day history for usage and forecast, time index??
//send electricity usage for each time push / home calculate electricity usage?
//Forecast