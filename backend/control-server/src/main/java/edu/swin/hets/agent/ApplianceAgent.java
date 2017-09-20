package edu.swin.hets.agent;
/*
import java.util.Iterator;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import edu.swin.hets.agent.other.BaseAgent;

public class ApplianceAgent extends BaseAgent
{
	//should be vector, should store enumeration instead of int
	int[] weather = new int[24];
	boolean on;
	int current, forecast;

	//initialize variables
	private void init()
	{
		on = true;
		current = 0;
		updateWeather();
	}

	@Override
	protected void setup()
	{
		super.setup();
		init();
	}

	private void updateWeather()
	{
		//send request for weather forecast
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("Weather Forecast");
		msg.addReceiver(new AID("WeatherAgent",AID.ISLOCALNAME));
		send(msg);
		//wait for result
		addBehaviour(new weatherForecastReceiver());
	}

	private class weatherForecastReceiver extends Behaviour
	{
		boolean finish=false;

		@Override
		public void action()
		{
			ACLMessage msg = blockingReceive();
			if(msg!=null)
			{
				//example message : weather,0
				//0=sunny, 1=cloudy
				weather[_current_time] = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
				//exit the behaviour
				finish = true;
			}
		}

		@Override
		public boolean done()
		{
			return finish;
		}
	}

	private void sendCurrentUsage()
	{
		updateCurrentUsage();
		//send
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("electricity current," + current);
		msg.addReceiver(new AID("HomeAgent", AID.ISLOCALNAME));
		send(msg);
	}

	//calculate current usage and update variable
	private void updateCurrentUsage()
	{
		current = 10;
	}

	private void sendForecastUsage()
	{
		updateForecastUsage();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("electricity forecast," + forecast);
		msg.addReceiver(new AID("HomeAgent", AID.ISLOCALNAME));
		send(msg);
	}

	//calculate forecast usage and update variable
	private void updateForecastUsage(){}

	private void sendElectricityRequest()
	{
		//send request for weather forecast
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("Weather Forecast");
		msg.addReceiver(new AID("WeatherAgent",AID.ISLOCALNAME));
		send(msg);
		//wait for result
		addBehaviour(new electricityReceiver());
	}

	private class electricityReceiver extends Behaviour
	{
		boolean finish=false;

		@Override
		public void action()
		{
			ACLMessage msg = blockingReceive();
			if(msg!=null)
			{
				//example message : electricity request,1
				//0=declined, 1=approved
				int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
				if(value==0){System.out.println("Request declined by HomeAgent");}
				else if(value==1)
				{
					on = true;
					//start current usage counting, error
					//myAgent.addBehaviour(new parallelTickerBehaviour());
				}
				//exit the behaviour
				finish = true;
			}
		}

		@Override
		public boolean done()
		{
			return finish;
		}
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
			}
			else if(on==false)
			{
				on = false;
				//stop current usage increment
			}
		}
	}

	//use parallel behaviour to count time
	//update current usage every minute
	private class parallelTickerBehaviour{}

	//TODO UnhandledMessage function
	@Override
	protected void UnhandledMessage(ACLMessage msg)
	{
		String senderName = msg.getSender().getLocalName();

		if(msg.getPerformative()==ACLMessage.REQUEST)
		{
			//UserAgent or HomeAgent turn on / off
			if(msg.getContent().contains("turn"))
			{
				//example message : turn,0
				//0=off, 1=on
				int intValue = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
				boolean booleanValue = false;
				if(intValue==0){booleanValue = false;}
				else if(intValue==1){booleanValue = true;}
				turn(booleanValue);
			}
			else{sendNotUndersood(msg,"Sorry?");}
		}
		else{sendNotUndersood(msg,"Sorry?");}
	}

	//TODO TimeExpiringIn function
	@Override
	protected void TimeExpiringIn(int expireTimeMS){}

	//TODO TimeExpired function
	@Override
	protected void TimeExpired(){}

	//TODO SaleMade function
	@Override
	protected void SaleMade(ACLMessage msg){}
}*/