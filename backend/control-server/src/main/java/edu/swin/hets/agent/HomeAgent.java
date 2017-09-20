package edu.swin.hets.agent;

//import java.lang.*;
/*
public class HomeAgent extends BaseAgent
{
	//number of appliances
	int n;
	//TODO use vector
	//electricity usage for each time slice for each appliances, should be vector for scalability
	int electricityUsage[][] = new int[24][n];
	//electricity forecast for next time slice for each appliance, should be vector for scalability
	int electricityForecast[][] = new int[24][n];
	//list of appliance agent name, should be vector
	String[] applianceName = new String[9];
	
	//init all variable value
	//TODO init function
	private void init()
	{
		//should get from argument
		n = 1;
		//should get from argument
		applianceName[0] = "lamp1";
	}
	
	private int getApplianceID(String name)
	{
		int i;
		for(i=0;i<n;i++)
			{if(name.equals(applianceName[i]))
				{return i;}}
		return -1;
	}
	
	@Override
	protected void setup()
	{
		super.setup();
		init();
		addBehaviour(new welcomeMessage());
		//addBehaviour(new msgReceivingBehaviour());
		System.out.println(getLocalName() + " setup is completed!");
	}
	
	private class welcomeMessage extends OneShotBehaviour
	{
		@Override
		public void action()
		{
			System.out.println(getLocalName() + " is now up and running!");
		}
	}
	
	private void sleep(int duration)
	{
		try{Thread.sleep(duration);}
		catch(InterruptedException e){e.printStackTrace();}
	}
	
	//forecast electricity needs for next day(x=1), next week(x=2), or next month(x=3)
	//TODO forecast function
	private int forecast(int x)
	{
		int result=0;
		//next day forecast
		if(x==1){}
		//next week forecast
		else if(x==2){}
		//next month forecast
		else if(x==3){}
		return result;
	}
	
	//when use too much electricity, Home agent could turn off unimportant appliances to prevent overload
	//TODO energySaverMode function
	private void energySaverMode(){}

	@Override
	protected void UnhandledMessage(ACLMessage msg)
	{
		String senderName = msg.getSender().getLocalName();
		
		if(msg.getPerformative()==ACLMessage.INFORM)
		{
			if(msg.getContent().contains("electricity"))
			{
				int applianceID = getApplianceID(senderName);
				int value = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(",")+1));
				if(msg.getContent().contains("current"))
				{
					//example message : electricity current,10
					//store in electricityUsage
					electricityUsage[_current_time][applianceID] = value;
				}
				else if(msg.getContent().contains("forecast"))
				{
					//example message : electricity forecast,12
					//store in electricity
					electricityForecast[_current_time][applianceID] = value;
				}
			}
			else
				{sendNotUndersood(msg,"Sorry?");}
		}
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
}

//TODO behaviour to negotiate price for buy & sell

*/