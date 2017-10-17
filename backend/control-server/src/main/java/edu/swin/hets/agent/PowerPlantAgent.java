package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;
import java.util.ArrayList;
/******************************************************************************
 *  Use: A simple example of a power plant class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Services Registered: "powerplant"
 *  Messages understood:
 *       - CFP : Used to ask for a request of electricity
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal, can also be used
 *                           invalidate an agreement that came back to late
 *             content Object: A PowerSaleProposal object
 *   Messages Sent:
 *       - NOT-UNDERSTOOD : Used to signal that there was no attached prop obj
 *              content: "no proposal found"
 *       - PROPOSE : Used to send out a proposal to someone
 *              content Object : A power sale proposal obj
 *****************************************************************************/
public class PowerPlantAgent extends BaseAgent {
    private static final int GROUP_ID = 1;
    private static String TYPE = "Power Plant";
    private double _money;
    private double _costOfProduction;
    private double _current_sell_price;
    private double _max_production;
    private double _current_production;
    private ArrayList<PowerSaleAgreement> _current_contracts;

    private MessageTemplate CFPMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));

    @Override
    protected void setup() {
        super.setup();
        _money = 500;
        _current_production = 10;
        _max_production = 1000;
        _costOfProduction = 0.5;
        _current_sell_price = 0.8;
        _current_contracts = new ArrayList<>();
        RegisterAMSService(getAID().getName(),"powerplant");
        addMessageHandler(CFPMessageTemplate, new CFPHandler());
        addMessageHandler(PropAcceptedMessageTemplate, new QuoteAcceptedHandler());
        addMessageHandler(PropRejectedMessageTemplate, new QuoteRejectedHandler());
    }

    // Update bookkeeping.
    protected void TimeExpired (){
        updateContracts();
        balanceBooks();
        LogVerbose(getName() + " is producing: " + _current_production);
    }

    protected String getJSON() {
        String json = "";
        try {
            json = new ObjectMapper().writeValueAsString(
                    new PowerPlantData(_current_sell_price, _current_production, getName()));
        }
        catch (JsonProcessingException e) {
            LogError("Error parsing data to json in " + getName() + " exeption thrown");
        }
        return json;
    }

    protected void TimePush(int ms_left) {

    }

    private void balanceBooks () {
        _current_contracts.forEach((agg) -> _money += agg.getCost() * agg.getAmount());
        _current_contracts.forEach((agg) -> _money -= agg.getAmount() * _costOfProduction);
    }

    private void updateContracts() {
        _current_production = 0;
        ArrayList<PowerSaleAgreement> toRemove = new ArrayList<>();
        // Filter out old contracts
        _current_contracts.stream().filter(
                (agg) -> agg.getEndTime() < _current_globals.getTime()).forEach(toRemove::add);
        _current_contracts.removeAll(toRemove);
        // Update how much we now need to produce.
        _current_contracts.forEach((agg) -> _current_production += agg.getAmount());
    }

    // Someone buying from us.
    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            // A request for a price on electricity
            PowerSaleProposal proposed = getPowerSalePorposal(msg);
            if (proposed.getAmount() > (_max_production - _current_production)) {
                // Cant sell that much electricity, don't bother putting a bit in.
                LogVerbose(getName() + " was asked to sell electricity than it can make.");
                return;
            }
            if (proposed.getCost() < 0) {
                // No amount set, set how much we will sell that quantity for.
                proposed.setCost(_current_sell_price);
            }
            else if (proposed.getCost() < _current_sell_price) {
                // To low a price, don't bother agreeing.
                //TODO Add negotiation here to try and make a more agreeable price.
                sendRejectProposalMessage(msg);
                return;
            }
            proposed.setSellerAID(getAID());
            ACLMessage response = msg.createReply();
            response.setPerformative(ACLMessage.PROPOSE);
            addPowerSaleProposal(response, proposed);
            response.setSender(getAID());
            send(response);
            LogVerbose(getName() + " sending a proposal to " + msg.getSender().getName());
        }
    }

    // Someone has rejected a quote
    private class QuoteRejectedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            if (GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()).match(msg)) {
                // Someone is rejecting a contract, remove it.
                PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
                ArrayList<PowerSaleAgreement> toRemove = new ArrayList<>();
                _current_contracts.stream().filter((agg) -> agg.equalValues(agreement)).forEach(toRemove::add);
                _current_contracts.removeAll(toRemove);
            }
            if (GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()).match(msg)) {
                // TODO, send back a better quote maybe?
            }
        }
    }

    // Someone agreeing to buy electricity from us.
    private class QuoteAcceptedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            // A quote we have previously made has been accepted.
            PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
            if (agreement.getAmount() > (_max_production - _current_production)) {
                // Cant sell that much electricity, send back error message.
                quoteNoLongerValid(msg);
                return;
            }
            LogDebug(getName() + " has made a deal");
            _current_contracts.add(agreement);
        }
    }

    private void quoteNoLongerValid(ACLMessage msg) {
        sendRejectProposalMessage(msg);
    }
    /******************************************************************************
     *  Use: Used by JSON serializing library to make JSON objects.
     *****************************************************************************/
    private class PowerPlantData implements Serializable{
        private String Name;
        private AgentData dat;
        PowerPlantData(double sell_price, double production, String name) {
            dat = new AgentData(sell_price, production, name);
            Name = name;

        }
        public String getid() { return Name; }
        public int getgroup() { return GROUP_ID; }
        public AgentData getagent() { return dat; }
        private class AgentData implements Serializable {
            private String Name;
            private double current_sell_price;
            private double current_production;
            AgentData (double sell_price, double production, String name) {
                current_sell_price = sell_price;
                current_production = production;
                Name = name;
            }
            public double getCurrent_Production() { return current_production; }
            public double getCurrent_Sell_Price() { return current_sell_price; }
            public String getName () { return Name.split("@")[0];}
        }
    }
}