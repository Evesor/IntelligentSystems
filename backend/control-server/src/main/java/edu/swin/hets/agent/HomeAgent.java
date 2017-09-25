package edu.swin.hets.agent;

import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.lang.*;

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
	//max watt of a house
	private int maxWatt;

	private MessageTemplate electricityCurrentMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity current"));

	private MessageTemplate electricityForecastMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity forecast"));

	private MessageTemplate electricityRequestMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("electricity"));
	
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
			watt[i] = 10;
			System.out.println(args[i]);
		}
		electricityUsage = new int[24][n];
		electricityForecast = new int[24][n];
		maxWatt = 10000;
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
		System.out.println(getLocalName() + " setup is completed!");
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
			System.out.println("electricity request delivered");
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
		else{return -1;}
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
		System.out.println("OSHU");
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		if(on==true){msg.setContent("on");}
		else if(on==false){msg.setContent("off");}
		msg.addReceiver(new AID(name,AID.ISLOCALNAME));
		send(msg);
	}

//	@Override
//	protected void UnhandledMessage(ACLMessage msg)
//	{
//		String senderName = msg.getSender().getLocalName();
//
//		if(msg.getPerformative()==ACLMessage.INFORM)
//		{
//			if(msg.getContent().contains("electricity"))
//			{
//				int applianceID = getApplianceID(senderName);
//				int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
//				if(msg.getContent().contains("current"))
//				{
//					//example message : electricity current,10
//					//store in electricityUsage
//					electricityUsage[_current_time][applianceID] = value;
//				}
//				else if(msg.getContent().contains("forecast"))
//				{
//					//example message : electricity forecast,12
//					//store in electricity
//					electricityForecast[_current_time][applianceID] = value;
//				}
//			}
//			else
//				{sendNotUndersood(msg,"Sorry?");}
//		}
//	}

	//TODO Override TimeExpired
	@Override
	protected void TimeExpired(){}

	//TODO Override TimePush
	@Override
	protected void TimePush(int ms_left){}

	//TODO Override getJSON
	@Override
	protected String getJSON(){return null;}
}

//TODO behaviour to negotiate price for buy & sell
//TODO receive request from user to turn on/off an appliance
//TODO receive request from user to initiate energy saver mode