package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.*;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;
import java.util.*;

/******************************************************************************
 *  Use: A simple example of a reseller agent class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Notes:
 *       To buy: this->CFP :: PROP->this :: this->ACC || this->REJ
 *       When selling: CFP->this :: this->PROP :: ACC->this || REJ->this
 *  Services Registered: "reseller"
 *  Messages Understood:
 *       - CFP : Used to negotiate purchasing electricity from reseller.
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted by
 *                           someone buying from us.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal from someone we
 *                           wanted to sell to.
 *             content Object: A PowerSaleProposal object
 *       - PROPOSAL : Used when someone wants to propose selling or buying
 *                    electricity from us.
 *             content Object: A PowerSaleProposal object
 *  Messages sent:
 *       - CFP : Used to negotiate purchasing electricity from power plant agent
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal
 *             content Object: A PowerSaleProposal object
 *       - PROPOSAL : Used to send a proposal back to a home user.
 *             content Object: A PowerSaleProposal object
 *****************************************************************************/
public class ResellerAgent extends BaseAgent {
    private static final int GROUP_ID = 2;
    private static final String TYPE = "Reseller Agent";
    private double _money;
    private double _current_sell_price;
    private double _current_by_price;
    private double _min_purchase_amount;
    private double _next_purchased_amount;
    private double _next_required_amount;
    private ArrayList<Double> _future_needs;
    private ArrayList<PowerSaleAgreement> _currentBuyAgreements;
    private ArrayList<PowerSaleAgreement> _currentSellAgreements;
    private HashMap<AID, ArrayList<PowerSaleAgreement>> _customerDB;
    private HashMap<AID, ArrayList<PowerSaleAgreement>> _sellerDB;
    private ArrayList<INegotiationStrategy> _currentNegotiations;


    private MessageTemplate CFPMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));
    private MessageTemplate PropMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));

    protected void setup() {
        super.setup();
        _current_by_price = 10;
        _current_sell_price = 1.0;
        _min_purchase_amount = 100;
        _next_required_amount = 200; //TODO let home users set demand.
        _money = 500;
        _currentBuyAgreements = new ArrayList<>();
        _currentSellAgreements = new ArrayList<>();
        _customerDB = new HashMap<>();
        _sellerDB = new HashMap<>();
        _currentNegotiations = new ArrayList<>();
        addMessageHandler(PropAcceptedMessageTemplate, new ProposalAcceptedHandler());
        addMessageHandler(PropRejectedMessageTemplate, new ProposalRejectedHandler());
        addMessageHandler(CFPMessageTemplate, new CFPHandler());
        addMessageHandler(PropMessageTemplate, new ProposalHandler());
        RegisterAMSService(getAID().getName(), "reseller");
    }

    protected String getJSON() {
        String json = "test";
        try {
            json = new ObjectMapper().writeValueAsString(
                    new ResellerAgentData(_current_by_price, _current_sell_price,_next_required_amount,
                            _next_purchased_amount, getName()));
        }
        catch (JsonProcessingException e) {
            LogError("Error parsing data to json in " + getName() + " exeption thrown");
        }
        return json;
    }

    // We are in a new time-slice, update bookeeping.
    protected void TimeExpired() {
        balanceBooks();
        updateContracts();
        // We now know how much we have bought and how much we need to buy, send out CFP's to get electricity we need.
        if (_next_required_amount > _next_purchased_amount) {
            sendBuyCFP();
        }
    }

    private void balanceBooks() {
        for (PowerSaleAgreement agg : _currentBuyAgreements) _money +=  agg.getAmount() * agg.getCost();
        for (PowerSaleAgreement agg : _currentSellAgreements) _money -=  agg.getAmount() * agg.getCost();
        if (_money < 0) {
            LogError(getName() + " has gone bankrupt!");
            //TODO, send message to main container to remove agent.
        }
    }

    private void updateContracts() {
        _next_purchased_amount = 0;
        _next_required_amount = 0;
        // Get rid of old contracts that are no longer valid
        ArrayList<PowerSaleAgreement> toRemove = new ArrayList<>();
        _currentBuyAgreements.stream().filter(
                (agg) -> agg.getEndTime() < _current_globals.getTime()).forEach(toRemove::add);
        _currentBuyAgreements.removeAll(toRemove);
        toRemove.clear();
        _currentSellAgreements.stream().filter(
                (agg) -> agg.getEndTime() < _current_globals.getTime()).forEach(toRemove::add);
        _currentSellAgreements.removeAll(toRemove);
        // Re calculate usage for this time slice
        for (PowerSaleAgreement agreement : _currentBuyAgreements) _next_purchased_amount += agreement.getAmount();
        for (PowerSaleAgreement agreement: _currentSellAgreements) _next_required_amount += agreement.getAmount();
        //TODO Remove later, for now just make random demand
        _next_required_amount = new Random().nextInt(300) + 100;
        if (_next_required_amount < _min_purchase_amount) {
            _next_required_amount = _min_purchase_amount;
        }
    }

    // Time is expiring, make sure we have purchased enough electricity
    protected void TimePush(int ms_left) {
        if (_next_required_amount > _next_purchased_amount ) {
            LogVerbose(getName() + " requires: " + _next_required_amount + " purchased: " +
                    _next_purchased_amount  + " has " + _money + " dollars");
            sendBuyCFP(); // We need to buy more electricity
        }
        // We have enough electricity do nothing.
    }

    // We want to to buy electricity
    private void sendBuyCFP() {
        DFAgentDescription[] powerPlants = getService("powerplant");
        IUtilityFunction currentUtil = new BasicUtility();
        _currentNegotiations.clear(); // We have just received a push or new timeSlice, clear list.
        //TODO make more complicated logic for initial offer.
        PowerSaleProposal prop = new PowerSaleProposal(
                _next_required_amount - _next_purchased_amount,1, getAID(), false);
        for (DFAgentDescription powerPlant : powerPlants) {
            // Make new negotiation for each powerPlant
            INegotiationStrategy strategy = new BasicNegotiator(currentUtil, getName(), powerPlant.getName().getName()
                    ,prop ,_current_globals.getTime());
            _currentNegotiations.add(strategy);
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setConversationId(UUID.randomUUID().toString());
            cfp.addReceiver(powerPlant.getName()); //CFP to each power plant
            prop.setBuyerAID(getAID());
            addPowerSaleProposal(cfp, prop);
            cfp.setSender(getAID());
            send(cfp);
        }
        LogDebug(getName() + " is sending cfp for: " + (_next_required_amount - _next_purchased_amount) );
    }

    // Someone is negotiating with us.
    private class ProposalHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            if (_next_required_amount > _next_purchased_amount){
                Optional<INegotiationStrategy> optional = _currentNegotiations.stream().filter(
                        (x) -> x.getOpponentName().equals(msg.getSender().getName())).findAny();
                if (! optional.isPresent()) {
                    LogError("We got a message from someone we were not negotiating with");
                    return;
                }
                INegotiationStrategy strategy = optional.get();
                PowerSaleProposal proposed = getPowerSalePorposal(msg);
                strategy.addNewProposal(proposed, false);
                IPowerSaleContract offer = strategy.getResponse();
                if (offer instanceof PowerSaleProposal) {
                    // Make counter offer
                    PowerSaleProposal counterProposal = (PowerSaleProposal) offer;
                    ACLMessage counterMsg = msg.createReply();
                    counterMsg.setPerformative(ACLMessage.PROPOSE);
                    addPowerSaleProposal(counterMsg, counterProposal);
                    LogDebug(getName() + " offered to pay " + counterProposal.getCost()  +
                            " for electricity negotiating with " + msg.getSender());
                    send(counterMsg);
                }
                else {
                    // Accept the contract
                    PowerSaleAgreement agreement = (PowerSaleAgreement) offer;
                    _currentBuyAgreements.add(agreement);
                    LogVerbose(getName() + " agreed to buy " + agreement.getAmount() + " electricity until " +
                            agreement.getEndTime() + " from " + agreement.getSellerAID().getName());
                    ACLMessage acceptMsg = msg.createReply();
                    acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    addPowerSaleAgreement(acceptMsg, agreement);
                    send(acceptMsg);
                    updateContracts();
                    LogDebug(getName() + " has purchased: " + _next_purchased_amount + " and needs: " + _next_required_amount);
                }
            }
        }
    }

    // Someone is buying electricity off us
    private class ProposalAcceptedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            //TODO, check this is a valid proposal still.
            PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
            _currentSellAgreements.add(agreement);
        }
    }

    // Someone has rejected a quote
    private class ProposalRejectedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            if (GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()).match(msg)) {
                // Someone is rejecting a contract, remove it.
                PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
                ArrayList<PowerSaleAgreement> toRemove = new ArrayList<>();
                 _currentBuyAgreements.stream().filter((agg) -> agg.equalValues(agreement)).forEach(toRemove::add);
                 _currentBuyAgreements.removeAll(toRemove);
                 toRemove.clear(); //Should not be required but what the hell.
                _currentSellAgreements.stream().filter((agg) -> agg.equalValues(agreement)).forEach(toRemove::add);
                _currentSellAgreements.removeAll(toRemove);
            }
            if (GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()).match(msg)) {
                // Don't care at the moment.
                // TODO, send back a better proposal maybe?
            }
        }
    }

    // Someone is wanting to buy electricity off us
    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            // A request for a price on electricity
            PowerSaleProposal proposed = getPowerSalePorposal(msg);
            if (_next_required_amount > _next_purchased_amount) {
                // We have sold all the electricity we have purchased.
                if (_current_globals.getTimeLeft() > (GlobalValues.lengthOfTimeSlice() * 0.75)) {
                    // %75 percent of a cycle left, make an offer at increased price.
                    if (proposed.getCost() < 0) {
                        // No cost added, make up one at +%25
                        proposed.setCost(_current_sell_price * 1.25);
                    }
                    else {
                        if (proposed.getCost() < _current_sell_price * 1.25) {
                            return; // There is already a price and it is to low
                            // TODO add negotiation of price.
                        }
                    }
                }
            }
            else {
                //Make offer of electricity to home
                if (proposed.getCost() > 0 && proposed.getCost() < _current_sell_price) {
                    // TODO add negotiation of price.
                    return;
                }
                if (proposed.getCost() < 0) {
                    proposed.setCost(_current_sell_price);
                }
                // else, leave the price alone, they have offered to pay more than we charge.
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
    /******************************************************************************
     *  Use: Used by getJson to output data to server.
     *****************************************************************************/
    private class ResellerAgentData implements Serializable {
            private AgentData data;
            private String Name;
            ResellerAgentData(double buy_price, double sell_price, double current_sales, double current_purchases, String name) {
                Name = name;
                data = new AgentData(buy_price, sell_price,current_sales, current_purchases, name);
            }
            public int getgroup() { return GROUP_ID; }
            public AgentData getagent() {return data; }
            public String getid() {return Name;}
            private class AgentData implements Serializable{
                private String Name;
                private double current_sell_price;
                private double current_buy_price;
                private double current_sales_volume;
                private double current_purchase_volume;
                AgentData (double buy_price, double sell_price, double current_sales, double current_purchases, String name){
                    current_sell_price = sell_price;
                    current_buy_price = buy_price;
                    current_sales_volume = current_sales;
                    current_purchase_volume = current_purchases;
                    Name = name;
                }
                public String getName () { return Name.split("@")[0];}
                public String getType () { return TYPE; }
                public double getCurrent_Sell_Price() { return current_sell_price; }
                public double getCurrent_Buy_Price() { return current_buy_price; }
                public double getCurrent_Purchase_Volume() { return current_purchase_volume; }
                public double getCurrent_Sales_Volume() { return current_sales_volume; }
        }
    }
    /******************************************************************************
     *  Use: A basic utility function to test new negotiation system.
     *  Notes: For the moment is lazy and coupled to reseller, will fix later.
     *****************************************************************************/
    private class BasicUtility implements IUtilityFunction {
        private double _costImperative = 5;
        private double _supplyImperative = 5;
        private double _timeImperative = 0.01;
        private double _idealPrice = 1;
        private GlobalValues _createdTime;

        BasicUtility () {
            _createdTime = _current_globals;
        }

        @Override
        public double evaluate(PowerSaleProposal proposal) {
            return (
                    Math.abs(proposal.getAmount() - (_next_required_amount - _next_purchased_amount) * _supplyImperative)
                            + Math.abs(proposal.getCost() - _idealPrice) * _costImperative
                            + Math.abs((GlobalValues.lengthOfTimeSlice() - _current_globals.getTimeLeft()) * _timeImperative));
        }

        @Override
        public boolean equals(IUtilityFunction utility) {
            return _createdTime == _current_globals;
        }
    }
}
