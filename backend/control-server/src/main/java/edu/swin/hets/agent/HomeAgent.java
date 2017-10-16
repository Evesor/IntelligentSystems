package edu.swin.hets.agent;

import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.lang.*;
import java.util.*;

public class HomeAgent extends BaseAgent
{
	public static String APPLIANCE_LIST_MAP_KEY = "HOME_AGENT_APPLIANCE_LIST";
	//number of appliances
	private int n;
	//TODO use vector
	//electricity usage for each time slice for each appliances, should be vector for scalability
	private int[][] electricityUsage;
	//electricity forecast for next time slice for each appliance, should be vector for scalability
	//private int[][] electricityForecast;
	private Map<String,Integer> electricityForecast;
	//appliances watt
	//private int[] watt;
	private Map<String, Integer> watt;
	//list of appliance agent name, should be vector
	//private String[] applianceName;
	private List<String> applianceName;
	//max watt threshold of a house
	private int maxWatt;
	//current state of all appliance
	//private boolean[] on;
	private Map<String, Boolean> on;
	private int energySaverWatt;
	private double _next_purchased_amount;
	private Vector<PowerSaleAgreement> _current_buy_agreements;
	private double _next_required_amount;
	private double _current_by_price;

	private MessageTemplate electricityCurrentMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity current"));

	private MessageTemplate electricityForecastMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity forecast"));

	private MessageTemplate electricityRequestMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("electricity"));

	private MessageTemplate PropMessageTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleProposal"));
	
	//init all variable value
	//TODO init function
	private void init()
	{
		Object[] args = getArguments();
		// The following commented code is an example of how to grab the list of appliances that this home agent owns.
		//
		//java.util.Map<String, Object> map = (java.util.Map<String, Object>) args[1];
		//java.util.List<String> appliances = (java.util.List<String>) map.get(APPLIANCE_LIST_MAP_KEY);
		//appliances.forEach(x -> System.out.println(getName() + ": " + x));
		n = args.length;
		//applianceName = new String[n];

		java.util.Map<String, Object> map = (java.util.Map<String, Object>) args[1];
		applianceName = (java.util.List<String>) map.get(APPLIANCE_LIST_MAP_KEY);

		//watt = new int[n];
		on = new HashMap<String, Boolean>();
		applianceName.forEach((appliance) -> {
			on.put(appliance, false);
			watt.put(appliance,10);
			electricityForecast.put(appliance,0);
		});
//		on = new boolean[n];
//		for(int i = 0; i < args.length; i++)
//		{
//			//applianceName[i] =  args[i].toString();
//			watt[i] = 10;//TODO get watt from appliance agent
//			//on[i] = false;
//			LogVerbose(args[i] + " has been added to " + getLocalName());
//		}
		//electricityUsage = new int[24][n];
		//electricityForecast = new int[24][n];
		maxWatt = 10000;
		energySaverWatt = 10;//TODO calculate energySaverWatt
		_current_buy_agreements = new Vector<PowerSaleAgreement>();
		_next_purchased_amount = 0;
		_current_by_price = 10;
		LogDebug(getLocalName() + " init is complete!");
	}
	
//	private int getApplianceID(String name)
//	{
//		int i;
//		for(i=0;i<n;i++)
//		{
//			if(name.equals(applianceName[i])){return i;}
//		}
//		return -1;
//	}
	
	@Override
	protected void setup()
	{
		super.setup();
		init();
		addBehaviour(new welcomeMessage());
		//addMessageHandler(electricityCurrentMT, new HomeAgent.CurrentHandler());
		addMessageHandler(electricityForecastMT, new HomeAgent.ForecastHandler());
		addMessageHandler(electricityRequestMT, new HomeAgent.electricityRequestHandler());
		addMessageHandler(PropMessageTemplate, new ProposalHandler());
		LogDebug(getLocalName() + " setup is complete!");
		turn("lamp1",true);
	}

	//example message : ACLMessage.INFORM,"electricity current,10"
//	private class CurrentHandler implements IMessageHandler
//	{
//		public void Handler(ACLMessage msg)
//		{
//			String senderName = msg.getSender().getLocalName();
//			int applianceID = getApplianceID(senderName);
//
//			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
//			//store in electricityUsage
//			//electricityUsage[_current_globals.getTime()][applianceID] = value;
//			LogDebug("current = " + value);
//		}
//	}

	//example message : ACLMessage.INFORM,"electricity forecast,10"
	private class ForecastHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			String senderName = msg.getSender().getLocalName();
			//int applianceID = getApplianceID(senderName);
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			//store in electricityUsage
			electricityForecast.put(senderName,value);
			LogDebug("forecast = " + forecast(1));
		}
	}

	//TODO electricityRequestHandler
	private class electricityRequestHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			LogDebug(getLocalName() + " received electricity request");
			//should check against maxWatt and decide
			boolean approve = true;
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			if((sumWatt()+value > maxWatt) || (sumWatt()+value > _next_purchased_amount))
				{approve = false;}
			ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
			if(approve == true)
				{//on[getApplianceID(msg.getSender().getLocalName())] = true;
				on.put(msg.getSender().getLocalName(),true);
				reply.setContent("electricity,1");}
			else{reply.setContent("electricity,0");}
			reply.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
			send(reply);
			//TODO below are just for simulation purposes, delete this later
			//sleep(5000);
			//energySaverMode();
		}
	}

	//sum of every on appliance watt
	private int sumWatt()
	{
		return applianceName.stream()
			.filter((appliance) -> on.get(appliance))
			.mapToInt((appliance) -> watt.get(appliance))
			.sum();
	}
	
	private class welcomeMessage extends OneShotBehaviour
	{
		@Override
		public void action(){LogVerbose(getLocalName() + " is now up and running!");}
	}
	
	private void sleep(int duration)
	{
		try{Thread.sleep(duration);}
		catch(InterruptedException e){e.printStackTrace();}
	}
	
	//forecast electricity needs for the next x hour
	//for now, next x hour forecast = x * next hour forecast
	//TODO calculate forecast
	private int forecast(int x)
	{
		int result=0;
		int i;
		//sum every appliance forecast
		//for(i=0;i<n;i++){result += electricityForecast[_current_globals.getTime()][i];}



		result =  applianceName.stream()
			.mapToInt((appliance) -> watt.get(appliance))
			.sum();

		result *= x;
		return result;
	}
	
	//when use too much electricity, Home agent could turn off unimportant appliances to prevent overload
	//this mode will turn on important appliances and turn off the other based on basic template
	//TODO energySaverMode function
	private void energySaverMode()
	{
		LogVerbose("initiating energy saver mode, " + _current_globals.getTimeLeft());
		turn("lamp1",true);
		//turn("heater",false);
		//turn("fridge",true);
	}

	private void turn(String name, boolean on)
	{
		LogDebug(getLocalName() + " is trying to turn " + name + " " + on);
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		if(on==true){msg.setContent("on");}
		else if(on==false)
			{
			//this.on[getApplianceID(name)] = false;

			this.on.put(name,false);


			msg.setContent("off");}
		msg.addReceiver(new AID(name,AID.ISLOCALNAME));
		msg.setSender(getAID());
		send(msg);
	}

	//TODO Override TimeExpired
	@Override
	protected void TimeExpired()
	{
		_next_purchased_amount -= sumWatt();
		_next_purchased_amount = 0;
		_next_required_amount = forecast(1)*1.5;
		Vector<PowerSaleAgreement> toRemove = new Vector<>();
		for (PowerSaleAgreement agreement : _current_buy_agreements) {
			if (agreement.getEndTime() < _current_globals.getTime()) {
				// No longer valid
				toRemove.add(agreement);
			}
		}
		for (PowerSaleAgreement rem: toRemove) {
			LogDebug("Removing a contract");
			_current_buy_agreements.removeElement(rem);
		}
		for (PowerSaleAgreement agreement : _current_buy_agreements) {
			// We have purchased this electricty.
			_next_purchased_amount += agreement.getAmount();
		}
		if (_next_required_amount > _next_purchased_amount) {
			sendCFP();
		}
	}

	//TODO Override TimePush
	@Override
	protected void TimePush(int ms_left)
	{
		LogDebug("Time Left : " + _current_globals.getTimeLeft());
		_next_purchased_amount -= sumWatt();
		//energySaverMode();
		if(sumWatt() > _next_purchased_amount)
		{
			if(energySaverWatt < _next_purchased_amount){energySaverMode();}
			else
			{
				int i;
				//for(i=0;i<n;i++){turn(applianceName[i],false);}

				applianceName.forEach((appliance) -> {
					turn(appliance,false);
				});

			}
		}
		else
		{
			energySaverMode();
		}

		//_next_required_amount = forecast(1)*1.5;
		if (_next_required_amount  > _next_purchased_amount) {
			LogVerbose("Required: " + _next_required_amount + " purchased: " + _next_purchased_amount);
			sendCFP(); // We need to buy more electricity
		}
		// We have enough electricity do nothing.
	}

	//TODO Override getJSON
	@Override
	protected String getJSON(){return "Not implemented";}

	private void sendCFP()
	{
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		DFAgentDescription[] resellers = getService("reseller");
		for (DFAgentDescription reseller : resellers) {
			cfp.addReceiver(reseller.getName()); //CFP to each reseller
		}
		//TODO make more complicated logic.
		/*_next_required_amount - _next_purchased_amount*/
		PowerSaleProposal prop = new PowerSaleProposal(_next_required_amount - _next_purchased_amount,
				1, getAID(), false);
		prop.setBuyerAID(getAID());
		LogVerbose("Sending a CFP to reseller for: " + prop.getAmount());
		try {
			cfp.setContentObject(prop);
		} catch (IOException e) {
			LogError("Could not attach a proposal to a message, exception thrown");
		}
		send(cfp);
		LogDebug("SEND CFP DONE");
	}

	// Someone is offering to sell us electricity
	private class ProposalHandler implements IMessageHandler {
		public void Handler(ACLMessage msg) {
			PowerSaleProposal proposed;
			try {
				proposed = (PowerSaleProposal) msg.getContentObject();
			} catch (UnreadableException e) {
				sendNotUndersood(msg, "no proposal found");
				return;
			}
			if (proposed.getCost() <= _current_by_price) {
				LogVerbose(getName() + " agreed to buy " + proposed.getAmount() + " electricity for " +
						proposed.getDuration() + " time slots");
				PowerSaleAgreement contract = new PowerSaleAgreement(proposed, _current_globals.getTime());
				_current_buy_agreements.add(contract);
				_next_purchased_amount += contract.getAmount();
				ACLMessage acceptMsg = msg.createReply();
				acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				addPowerSaleAgreement(acceptMsg, contract);
				send(acceptMsg);
			}
		}
	}
}

//TODO behaviour to negotiate price for buy & sell
//TODO receive request from user to turn on/off an appliance
//TODO receive request from user to initiate energy saver mode