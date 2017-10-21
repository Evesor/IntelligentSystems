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
	//private int n;
	//electricity forecast for next time slice for each appliance
	private Map<String,Integer> electricityForecast;
	//wattage of each appliance
	private Map<String, Integer> watt;
	//list of appliance name
	private List<String> applianceName;
	//max watt threshold of a house
	private int maxWatt;
	//current state of each appliance
	private Map<String, Boolean> on;
	//wattage used in energy saver mode
	private int energySaverWatt;
	//purchased electricity for next time slice
	private double _next_purchased_amount;
	//purchased contracts
	private Vector<PowerSaleAgreement> _current_buy_agreements;
	//electricity required in next time slice
	private double _next_required_amount;
	//maximum acceptable buy price
	private double _current_by_price;

	private MessageTemplate electricityForecastMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity forecast"));//TODO change message string into "forecast"

	private MessageTemplate electricityRequestMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("electricity"));

	private MessageTemplate PropMessageTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleProposal"));
	
	//initialize all variable value
	private void init()
	{
		Object[] args = getArguments();
		java.util.Map<String, Object> map = (java.util.Map<String, Object>) args[1];
		applianceName = (java.util.List<String>) map.get(APPLIANCE_LIST_MAP_KEY);
		on = new HashMap<String,Boolean>();
		watt = new HashMap<String,Integer>();
		electricityForecast = new HashMap<String,Integer>();
		applianceName.forEach((appliance) -> {
			on.put(appliance, false);
			watt.put(appliance,10);//TODO get appliance watt from JSON
			electricityForecast.put(appliance,0);//TODO check sequence of execution
			LogVerbose(appliance + " has been added to " + getLocalName());
		});
		maxWatt = 10000;
		energySaverWatt = 10;//TODO calculate energySaverWatt
		_current_buy_agreements = new Vector<PowerSaleAgreement>();
		_next_purchased_amount = 0;
		_current_by_price = 10;
		LogDebug(getLocalName() + " init is complete!");
	}
	
	@Override
	protected void setup()
	{
		super.setup();
		init();
		addBehaviour(new welcomeMessage());
		addMessageHandler(electricityForecastMT, new HomeAgent.ForecastHandler());
		addMessageHandler(electricityRequestMT, new HomeAgent.electricityRequestHandler());
		addMessageHandler(PropMessageTemplate, new ProposalHandler());
		LogDebug(getLocalName() + " setup is complete!");
		turn("lamp1",true);
	}

	//example message : ACLMessage.INFORM from appliance,"electricity forecast,10"
	//receive forecast message and save in electricityForecast
	private class ForecastHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			String senderName = msg.getSender().getLocalName();
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			electricityForecast.put(senderName,value);
			LogDebug("forecast = " + forecast(1));
		}
	}

	//TODO electricityRequestHandler
	//example message : ACLMessage.REQUEST from appliance, "electricity,10"
	//receive electricity request from appliance
	//compare with maxWatt & _next_purchased_amount to decide
	//send decision as reply
	private class electricityRequestHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			LogDebug(getLocalName() + " received electricity request");
			boolean approve = true;
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			if((sumWatt()+value > maxWatt) || (sumWatt()+value > _next_purchased_amount))
				{approve = false;}
			ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
			if(approve == true)
				{on.put(msg.getSender().getLocalName(),true);
				reply.setContent("electricity,1");}
			else{reply.setContent("electricity,0");}
			reply.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
			send(reply);
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

	//TODO do I need to sleep?
	private void sleep(int duration)
	{
		try{Thread.sleep(duration);}
		catch(InterruptedException e){e.printStackTrace();}
	}
	
	//sum of all appliance electricity forecast in the next x hour
	//for now, next x hour forecast = x * next hour forecast
	//TODO calculate forecast
	private int forecast(int x)
	{
		int result = applianceName.stream()
			.mapToInt((appliance) -> watt.get(appliance))
			.sum();

		return x*result;
	}
	
	//when use too much electricity, Home agent could turn off unimportant appliances to prevent overload
	//this mode will turn on important appliances and turn off the other based on basic template
	private void energySaverMode()
	{
		LogVerbose("initiating energy saver mode, " + _current_globals.getTimeLeft());
		turn("lamp1",true);
		//turn("heater1",false);
		//turn("fridge1",true);
	}

	//send request message to turn an appliance on or off
	private void turn(String name, boolean on)
	{
		LogDebug(getLocalName() + " is trying to turn " + name + " " + on);
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		if(on==true){msg.setContent("on");}
		else if(on==false)
			{this.on.put(name,false);
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
			sendBuyCFP();
		}
	}

	//TODO Override TimePush
	@Override
	protected void TimePush(int ms_left)
	{
		LogDebug("Time Left : " + _current_globals.getTimeLeft());
		_next_purchased_amount -= sumWatt();
		if(sumWatt() > _next_purchased_amount)
		{
			if(energySaverWatt < _next_purchased_amount){energySaverMode();}
			else
			{
				int i;
				applianceName.forEach((appliance) -> {
					turn(appliance,false);
				});
			}
		}
		//TODO for demo only, delete this later
		else
		{
			energySaverMode();
		}

		//_next_required_amount = forecast(1)*1.5;
		if (_next_required_amount  > _next_purchased_amount) {
			LogVerbose("Required: " + _next_required_amount + " purchased: " + _next_purchased_amount);
			sendBuyCFP(); // We need to buy more electricity
		}
		// We have enough electricity do nothing.
	}

	//TODO Override getJSON
	@Override
	protected String getJSON(){return "Not implemented";}

	//send buy CFP
	private void sendBuyCFP()
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

	private int sentCounter;
	private Vector<PowerSaleProposal> receivedBuyProposal;
	private void sendSellCFP()
	{
		sentCounter = 0;
		receivedBuyProposal = null;
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		DFAgentDescription[] resellers = getService("reseller");
		for(DFAgentDescription reseller : resellers)
			{cfp.addReceiver(reseller.getName());
			sentCounter++;}
		PowerSaleProposal prop = new PowerSaleProposal(1,1,getAID(),true);
		//prop.setSellerAID(getAID());
		addPowerSaleProposal(cfp,prop);
		send(cfp);
	}

	// Someone is offering to sell us electricity
//	private class buyProposalHandler implements IMessageHandler {
//		public void Handler(ACLMessage msg) {
//			if(receivedBuyProposal.length < sentCounter){receivedBuyProposal.add( += msg;}
//			if(sentCounter==receivedBuyProposal.length)
//			{
//				//pick cheapest deal
//				double min = receivedBuyProposal[0].getCost();
//				int indexMin = 0;
//				int i;
//				for(i=1;i<=receivedBuyProposal.length-1;i++)
//					{if(receivedBuyProposal[i].getCost() < min)
//						{min = receivedBuyProposal[i].getCost();
//						indexMin = i;}}
//
//				//TODO send accept
//				//TODO send reject
//			}
//		}
//	}
}

//TODO behaviour to negotiate price for buy & sell
//TODO receive request from user to turn on/off an appliance
//TODO receive request from user to initiate energy saver mode