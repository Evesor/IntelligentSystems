package edu.swin.hets.agent;

import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;
import java.util.Vector;

public class ApplianceAgent extends BaseAgent
{
	boolean on;
	//TODO current array
	//should be vector
	//private Vector<Integer> current;
	int[] current = new int[48];
	//TODO forecast array
	//should be vector, should store enumeration instead of int
	//private Vector<Integer> forecast;
	int[] forecast = new int[48];
	int watt;

	//turn on this appliance
	//ACLMessage.REQUEST, "on"
	private MessageTemplate R_On = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("on"));

	//turn off this appliance
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
		on = false;
		//current = new Vector<Integer>();
		//forecast = new Vector<Integer>();
		int i;
		for(i=0;i<24;i++)
		{
			current[i] = 0;
			forecast[i] = 0;
		}
		Object[] args = getArguments();
		//watt = Integer.parseInt(args[0].toString());
		List<String> argument = (List<String>) args[0];
		watt = Integer.parseInt(argument.get(0));

		updateForecastUsage();
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

	private class OnHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg){turn(true);}
	}

	private class OffHandler implements IMessageHandler
	{
		public void Handler(ACLMessage msg){turn(false);}
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
		msg.setContent("electricity current," + current[_current_globals.getTime()]);
		msg.addReceiver(new AID("home1", AID.ISLOCALNAME));
		send(msg);
	}

	private void sendForecastUsage()
	{
		updateForecastUsage();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("electricity forecast," + forecast[_current_globals.getTime()]);
		msg.addReceiver(new AID("home1", AID.ISLOCALNAME));
		send(msg);
	}

	//TODO updateForcastUsage function
	//calculate forecast usage and update variable
	private void updateForecastUsage(){forecast[_current_globals.getTime()] = watt*5;}

	private void sendElectricityRequest()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("electricity," + watt);
		msg.addReceiver(new AID("home1",AID.ISLOCALNAME));
		send(msg);
	}

	private void turn(boolean on)
	{
		//compare with current state
		if(this.on!=on)
		{
			if(on==true)
			{
				//send electricity request to home agent
				//home agent check current usage with max usage
				//if current + request < max usage, approve
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
			current[_current_globals.getTime()] += watt;
			LogDebug("current : " + current[_current_globals.getTime()]);
			sendForecastUsage();
		}
	}

	@Override
	protected void TimePush(int ms_left)
	{
		//count electricity usage
		if(on == true)
		{
			current[_current_globals.getTime()] += watt;
			LogDebug("current : " + current[_current_globals.getTime()]);
		}
	}

	//TODO Override getJSON
	@Override
	protected String getJSON(){return "Not implimented";}
}
//TODO other list
//appliance send electricity request / home approve before turning it on?
//1 day history for usage and forecast, time index??
//send electricity usage for each time push / home calculate electricity usage?