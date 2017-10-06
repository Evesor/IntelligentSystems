package edu.swin.hets.agent;

import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ApplianceAgent extends BaseAgent
{
	//should be vector, should store enumeration instead of int
//	int[] weather = new int[24];//_current_globals.getWeather();
	boolean on;
	int[] current = new int[24];
	int[] forecast = new int[24];
	int watt;
	UsageCounterBehaviour ucb;

	private MessageTemplate onMessageTemplate = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("on"));

	private MessageTemplate offMessageTemplate = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		GoodMessageTemplates.ContatinsString("off"));


	private MessageTemplate electricityReceiverMT = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		GoodMessageTemplates.ContatinsString("electricity request"));

	//initialize variables
	private void init()
	{
		on = false;
		int i;
		for(i=0;i<24;i++)
		{
			current[i] = 0;
			forecast[i] = 0;
		}
		watt = 10;
		ucb = new UsageCounterBehaviour(this,1000);
		updateForecastUsage();
	}

	@Override
	protected void setup()
	{
		super.setup();
		init();
		addMessageHandler(onMessageTemplate, new ApplianceAgent.OnHandler());
		addMessageHandler(offMessageTemplate, new ApplianceAgent.OffHandler());
		addMessageHandler(electricityReceiverMT, new ApplianceAgent.ElectricityHandler());
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
			//example message : electricity request,1
			//0=declined, 1=approved
			System.out.println(getLocalName() + " gets permission to use electricity");
			int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
			if(value==0){System.out.println(getLocalName() + " electricity request declined");}
			else if(value==1)
			{
				System.out.println(getLocalName() + " is now on");
				on = true;
				counterOn();
			}
		}
	}

	//start usage counter
	private void counterOn()
	{
		ucb = new UsageCounterBehaviour(this,1000);
		addBehaviour(ucb);
	}

	private void sendCurrentUsage()
	{
		//send
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

	//TODO updateForestUsage function
	//calculate forecast usage and update variable
	private void updateForecastUsage(){forecast[_current_globals.getTime()] = 50;}

	private void sendElectricityRequest()
	{
		//send request for weather forecast
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("electricity," + watt);
		msg.addReceiver(new AID("home1",AID.ISLOCALNAME));
		send(msg);
	}

	private void turn(boolean on)
	{
		if(this.on!=on)
		{
			if(on==true)
			{
				//send electricity request to home agent
				//home agent check current usage with max usage
				//if current + request < max usage, approve
				sendElectricityRequest();
				System.out.println(getLocalName() + " sent an electricity request");
			}
			else if(on==false)
			{
				this.on = false;
				//stop current usage increment
				ucb.stop();
				System.out.println(getLocalName() + " is now off");
			}
		}
	}

	//TODO usageCounterBehaviour function
	//?use parallel behaviour to count time?
	//update current usage every real second
	private class UsageCounterBehaviour extends TickerBehaviour
	{
		public UsageCounterBehaviour(Agent a, long period){super(a, period);}

		@Override
		protected void onTick()
		{
			current[_current_globals.getTime()] += watt;
			System.out.println("current : " + current[_current_globals.getTime()]);
		}
	}

	//TODO Override TimeExpired
	@Override
	protected void TimeExpired(){}

	//TODO Override TimePush
    @Override
    protected void TimePush(int ms_left){}

    //TODO Override getJSON
    @Override
    protected String getJSON(){
    	return "Not implimented";
    }
}