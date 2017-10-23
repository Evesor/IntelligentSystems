package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.*;
import edu.swin.hets.helper.negotiator.HoldForFirstOfferPrice;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.lang.*;
import java.util.*;
/******************************************************************************
 *  Use: The primary home agent.
 *  Messages Understood:
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted by
 *                           someone selling or buying from us.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal from someone we
 *                           wanted to buy / sell to.
 *             content Object: A PowerSaleProposal object
 *       - PROPOSAL : Used when someone wants to negotiate selling or buying
 *                    electricity from us.
 *             content Object: A PowerSaleProposal object
 *  Messages sent:
 *       - CFP : Used to negotiate purchasing electricity from reseller agents
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal and request to
 *       					 cancel ongoing negotiations with that agent.
 *             content Object: A PowerSaleProposal object
 *       - PROPOSAL : Used to send a counter proposal back to a reseller.
 *             content Object: A PowerSaleProposal object
 *****************************************************************************/
public class HomeAgent extends NegotiatingAgent
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
	private ArrayList<INegotiationStrategy> _currentNegotiations;
	private Vector<PowerSaleAgreement> _current_sell_agreements;

	private MessageTemplate electricityForecastMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity forecast"));//TODO change message string into "forecast"

	private MessageTemplate electricityRequestMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("electricity"));

	private MessageTemplate PropMessageTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));

	private MessageTemplate QuoteAcceptedTemplate = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
		GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()));
	
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
		_current_sell_agreements = new Vector<PowerSaleAgreement>();
		_next_purchased_amount = 0;
		_current_by_price = 10;
		LogDebug(getLocalName() + " init is complete!");
		_currentNegotiations = new ArrayList<>();
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
		addMessageHandler(QuoteAcceptedTemplate, new ProposalAcceptedHandler());
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

		for (PowerSaleAgreement agreement : _current_sell_agreements) {
			if (agreement.getEndTime() < _current_globals.getTime()) {
				// No longer valid
				toRemove.add(agreement);
			}
		}
		for (PowerSaleAgreement rem: toRemove) {
			LogDebug("Removing a contract");
			_current_sell_agreements.removeElement(rem);
		}
		for (PowerSaleAgreement agreement : _current_sell_agreements) {
			// We have purchased this electricty.
			_next_purchased_amount -= agreement.getAmount();
		}

		if(_next_required_amount > _next_purchased_amount) {
			sendBuyCFP();
		}
		if(_next_required_amount < _next_purchased_amount) {
			sendSellCFP();
		}
	}

	//TODO Override TimePush
	@Override
	protected void TimePush(int ms_left)
	{
		//LogDebug("Time Left : " + _current_globals.getTimeLeft());
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

		if(_next_required_amount < _next_purchased_amount) {
			sendSellCFP();
		}
	}

	//TODO Override getJSON
	@Override
	protected String getJSON(){
		String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(
					new HomeAgentData());
		}
		catch (JsonProcessingException e) {
			LogError("Error parsing data to json in " + getName() + " exeption thrown");
		}
		return json;}

	//send buy CFP
	private void sendBuyCFP()
	{
//		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
//		DFAgentDescription[] resellers = getService("reseller");
//		for (DFAgentDescription reseller : resellers) {
//			cfp.addReceiver(reseller.getName()); //CFP to each reseller
//		}
//		//TODO make more complicated logic.
//		/*_next_required_amount - _next_purchased_amount*/
//		PowerSaleProposal prop = new PowerSaleProposal(_next_required_amount - _next_purchased_amount,
//				1, getAID(), false);
//		prop.setBuyerAID(getAID());
//		LogVerbose("Sending a CFP to reseller for: " + prop.getAmount());
//		try {
//			cfp.setContentObject(prop);
//		} catch (IOException e) {
//			LogError("Could not attach a proposal to a message, exception thrown");
//		}
//		send(cfp);
//		LogDebug("SEND CFP DONE");

		double toBuy = _next_required_amount - _next_purchased_amount;
		PowerSaleProposal prop = new PowerSaleProposal(toBuy,1,getAID(),false);
		prop.setBuyerAID(getAID());
		DFAgentDescription[] resellers = getService("reseller");
		for(DFAgentDescription reseller : resellers)
		{
			ACLMessage sent = sendCFP(prop, reseller.getName());
			INegotiationStrategy strategy = new HoldForFirstOfferPrice(prop,sent.getConversationId()
					,reseller.getName().getName(),_current_globals.getTime(), 20);
			_currentNegotiations.add(strategy);
			LogDebug("sending buy CFP");
		}
	}

	// Someone is buying electricity off us
	private class ProposalAcceptedHandler implements IMessageHandler {
		public void Handler(ACLMessage msg) {
			//TODO, check this is a valid proposal still.
			PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
			if (agreement.getSellerAID().getName().equals(getName())) _current_sell_agreements.add(agreement);
			else _current_buy_agreements.add(agreement);
			sendSaleMade(agreement);
			LogDebug("Accepted a prop from: " + msg.getSender().getName() + " for " + agreement.getAmount() +
					" @ " + agreement.getCost());
		}
	}

	// Someone is offering to sell us electricity
	private class ProposalHandler implements IMessageHandler {
		public void Handler(ACLMessage msg) {
			//_currentNegotiations.forEach((neg) -> LogDebug("Negotiating with: " + neg.getOpponentName()));
			Optional<INegotiationStrategy> opt = _currentNegotiations.stream().filter(
				(agg) -> agg.getOpponentName().equals(msg.getSender().getName())).findAny();
			if (opt.isPresent()) {
				//LogError("opt PRESENT" + msg.getSender());
				INegotiationStrategy negotiation = opt.get();
				PowerSaleProposal prop = getPowerSalePorposal(msg);
				negotiation.addNewProposal(prop, false);
				Optional<IPowerSaleContract> response = negotiation.getResponse();
				if (! response.isPresent()) { //End negotiation
					sendRejectProposalMessage(msg, prop);
					_currentNegotiations.remove(negotiation);
					LogDebug("has stopped negotiating with " + msg.getSender());
					return;
				}
				if(response.get() instanceof PowerSaleProposal) {
					// We should send back a counter proposal.
//					PowerSaleProposal counterProposal = (PowerSaleProposal) response;
//					negotiation.addNewProposal(counterProposal, true);
//					_currentNegotiations.add(negotiation);
//					ACLMessage replyMsg = msg.createReply();
//					replyMsg.setPerformative(ACLMessage.PROPOSE);
//					addPowerSaleProposal(replyMsg, counterProposal);
//					send(replyMsg);
				}
				else {
					// We should accept the contract.
					_currentNegotiations.clear();
					PowerSaleAgreement contract = new PowerSaleAgreement(prop, _current_globals.getTime());
					LogVerbose(" has accepted a contract from " + msg.getSender().getName() +
							" for " + contract.getCost());
					_current_sell_agreements.add(contract);
					_next_purchased_amount -= contract.getAmount();
					ACLMessage acceptMsg = msg.createReply();
					acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					addPowerSaleAgreement(acceptMsg, contract);
					send(acceptMsg);
				}
			}
			else{LogError("opt is not present : " + msg.getSender().getLocalName());}
		}
	}

	private void sendSellCFP()
	{
		double toSell = _next_purchased_amount - _next_required_amount;
		PowerSaleProposal prop = new PowerSaleProposal(toSell,1,getAID(),true);
		prop.setSellerAID(getAID());
		DFAgentDescription[] resellers = getService("reseller");
		for(DFAgentDescription reseller : resellers)
		{
			ACLMessage sent = sendCFP(prop, reseller.getName());
			INegotiationStrategy strategy = new HoldForFirstOfferPrice(prop,sent.getConversationId()
					, reseller.getName().getName(),
				 _current_globals.getTime(), 20);
			_currentNegotiations.add(strategy);
		}
	}
	/******************************************************************************
	 *  Use: Used by JSON serializing library to make JSON objects.
	 *****************************************************************************/
	private class HomeAgentData {
		public Double getNextRequiredAmount () { return  _next_required_amount; }
		public Double getNextPurchasedAmoutnt () { return _next_purchased_amount; }
	}
}

//TODO behaviour to negotiate price for buy & sell
//TODO receive request from user to turn on/off an appliance
//TODO receive request from user to initiate energy saver mode