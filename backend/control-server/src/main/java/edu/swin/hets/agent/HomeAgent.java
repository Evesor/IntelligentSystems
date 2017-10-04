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
import java.util.Vector;

public class HomeAgent extends BaseAgent
{
	//number of appliances
	private int n;
	//TODO use vector
	//electricity usage for each time slice for each appliances, should be vector for scalability
	private int[][] electricityUsage;
	//electricity forecast for next time slice for each appliance, should be vector for scalability
	private int[][] electricityForecast;
	//appliances watt
	private int[] watt;
	//list of appliance agent name, should be vector
	private String[] applianceName;
	//max watt threshold  of a house
	private int maxWatt;
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
		n = args.length;
		applianceName = new String[9];
		watt = new int[n];
		for(int i = 0; i < args.length; i++)
		{
			applianceName[i] =  args[i].toString();
			watt[i] = 10;//TODO get watt from appliance agent
			System.out.println(args[i] + " has been added to " + getLocalName());
		}
		electricityUsage = new int[24][n];
		electricityForecast = new int[24][n];
		maxWatt = 10000;
		_current_buy_agreements = new Vector<PowerSaleAgreement>();
		_next_purchased_amount = 200;
		_current_by_price = 10;
		System.out.println(getLocalName() + " init is complete!");
	}
	
	private int getApplianceID(String name)
	{
		int i;
		for(i=0;i<n;i++)
		{
			if(name.equals(applianceName[i])){return i;}
		}
		return -1;
	}
	
	@Override
	protected void setup()
	{
		super.setup();
		init();
		addBehaviour(new welcomeMessage());
		addMessageHandler(electricityCurrentMT, new HomeAgent.CurrentHandler());
		addMessageHandler(electricityForecastMT, new HomeAgent.ForecastHandler());
		addMessageHandler(electricityRequestMT, new HomeAgent.electricityRequestHandler());
		addMessageHandler(PropMessageTemplate, new ProposalHandler());
		System.out.println(getLocalName() + " setup is complete!");
		turn("lamp1",true);
	}

	//example message : ACLMessage.INFORM,"electricity current,10"
	private class CurrentHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			String senderName = msg.getSender().getLocalName();
			int applianceID = getApplianceID(senderName);
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			//store in electricityUsage
			electricityUsage[_current_globals.getTime()][applianceID] = value;
			System.out.println("current = " + value);
		}
	}

	//example message : ACLMessage.INFORM,"electricity forecast,10"
	private class ForecastHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			String senderName = msg.getSender().getLocalName();
			int applianceID = getApplianceID(senderName);
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			//store in electricityUsage
			electricityForecast[_current_globals.getTime()][applianceID] = value;
			System.out.println("forecast = " + forecast(1));
		}
	}

	//example message
	private class electricityRequestHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg)
		{
			System.out.println(getLocalName() + " received electricity request");
			//should check against maxWatt and decide
			ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
			reply.setContent("electricity request,1");
			reply.addReceiver(new AID("lamp1", AID.ISLOCALNAME));
			send(reply);
			sleep(5000);
			energySaverMode();
		}
	}
	
	private class welcomeMessage extends OneShotBehaviour
	{
		@Override
		public void action(){System.out.println(getLocalName() + " is now up and running!");}
	}
	
	private void sleep(int duration)
	{
		try{Thread.sleep(duration);}
		catch(InterruptedException e){e.printStackTrace();}
	}
	
	//forecast electricity needs for next hour(x=1), next day(x=2), or next week(x=3)
	//for now, next day forecast = 24 * next hour forecast
	//TODO calculate forecast
	private int forecast(int x)
	{
		int result=0;
		int i;
		for(i=0;i<n;i++){result += electricityForecast[_current_globals.getTime()][i];}
		//next hour forecast
		if(x==1){return result;}
		//next day forecast
		else if(x==2){return result*24;}
		//next week forecast
		else if(x==3){return result*168;}
		//error
		else{return 100;}
	}
	
	//when use too much electricity, Home agent could turn off unimportant appliances to prevent overload
	//this mode will turn on important appliances and turn off the other based on basic template
	//TODO energySaverMode function
	private void energySaverMode()
	{
		System.out.println("initiating energy saver mode");
		turn("lamp1",false);
		//turn("heater",false);
		//turn("fridge",true);
	}

	private void turn(String name, boolean on)
	{
		System.out.println(getLocalName() + " is trying to turn " + name + " " + on);
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		if(on==true){msg.setContent("on");}
		else if(on==false){msg.setContent("off");}
		msg.addReceiver(new AID(name,AID.ISLOCALNAME));
		send(msg);
	}

	//TODO Override TimeExpired
	@Override
	protected void TimeExpired()
	{
		System.out.println("OSSU");
		_next_purchased_amount = 0;
		_next_required_amount = forecast(1);
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
		if (_next_required_amount - _next_purchased_amount > 0.1) {
			sendCFP();
			System.out.println("CFP sent");
		}
	}

	//TODO Override TimePush
	@Override
	protected void TimePush(int ms_left)
	{
		_next_required_amount = forecast(1);
		if (_next_required_amount - _next_purchased_amount > 0.1) {
			LogVerbose("Required: " + _next_required_amount + " purchased: " + _next_purchased_amount);
			sendCFP(); // We need to buy more electricity
		}
		// We have enough electricity do nothing.
	}

	//TODO Override getJSON
	@Override
	protected String getJSON(){return null;}

	private void sendCFP()
	{
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		DFAgentDescription[] resellers = getService("reseller");
		for (DFAgentDescription reseller : resellers) {
			cfp.addReceiver(reseller.getName()); //CFP to each reseller
		}
		//TODO make more complicated logic.
		/*_next_required_amount - _next_purchased_amount*/
		PowerSaleProposal prop = new PowerSaleProposal(_next_required_amount - _next_purchased_amount,4);
		prop.setBuyerAID(getAID());
		try {
			cfp.setContentObject(prop);
		} catch (IOException e) {
			LogError("Could not attach a proposal to a message, exception thrown");
		}
		send(cfp);
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
			if (proposed.getCost() <= (_current_by_price * proposed.getAmount())) {
				LogVerbose(getName() + " agreed to buy " + proposed.getAmount() + " electricity for " +
						proposed.getDuration() + " time slots");
				PowerSaleAgreement contract = new PowerSaleAgreement(proposed, _current_globals.getTime());
				_current_buy_agreements.add(contract);
				_next_purchased_amount += contract.getAmount();
				ACLMessage acceptMsg = msg.createReply();
				acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				try {
					acceptMsg.setContentObject(contract);
				} catch (IOException e) {
					LogError("Could not add a contract to message, exception thrown");
				}
				send(acceptMsg);
			}
		}
	}
}


//TODO behaviour to negotiate price for buy & sell
//TODO receive request from user to turn on/off an appliance
//TODO receive request from user to initiate energy saver mode