package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.*;
import edu.swin.hets.helper.negotiator.HoldForFirstOfferPrice;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
public class ResellerAgent extends NegotiatingAgent {
    private static final double FINE_FOR_FAILURE_TO_FULFILL_CONTRACT = 2;
    private static final int GROUP_ID = 2;
    private static final String TYPE = "Reseller Agent";
    private double _money;
    private double _currentSellPrice;
    private double _currentByPrice;
    private double _nextPurchasedAmount;
    private double _nextRequiredAmount;
    private ArrayList<PowerSaleAgreement> _currentBuyAgreements;
    private ArrayList<PowerSaleAgreement> _currentSellAgreements;
    private HashMap<AID, ArrayList<PowerSaleAgreement>> _customerDB;
    private HashMap<AID, ArrayList<PowerSaleAgreement>> _sellerDB;
    private ArrayList<INegotiationStrategy> _currentNegotiations;
    private List<String> _strategyParams;


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
        _currentByPrice = 1;
        _currentSellPrice = 1.0;
        _nextRequiredAmount = 0;
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
        _strategyParams = (List<String>) getArguments()[0];
        if (_strategyParams.size() > 0) {
            _strategyParams.forEach((a) -> LogDebug(" was passed: " + a));
        }
    }

    protected String getJSON() {
        String json = "test";
        try {
            json = new ObjectMapper().writeValueAsString(
                    new ResellerAgentData(_currentByPrice, _currentSellPrice, _nextRequiredAmount,
                            _nextPurchasedAmount, getName()));
        }
        catch (JsonProcessingException e) {
            LogError("Error parsing data to json in " + getName() + " exeption thrown");
        }
        return json;
    }

    // We are in a new time-slice, update bookkeeping.
    protected void TimeExpired() {
        if (_nextRequiredAmount > _nextPurchasedAmount) {
            double fine = FINE_FOR_FAILURE_TO_FULFILL_CONTRACT * (_nextRequiredAmount - _nextPurchasedAmount);
            LogError(getName() + " failed to fulfill all its contracts and was fined: " + fine);
            _money -= fine;
        }
        balanceBooks();
        updateContracts();
        // We now know how much we have bought and how much we need to buy, send out CFP's to get electricity we need.
        if (_nextRequiredAmount > _nextPurchasedAmount) {
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
        _nextPurchasedAmount = 0;
        _nextRequiredAmount = 0;
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
        for (PowerSaleAgreement agreement : _currentBuyAgreements) _nextPurchasedAmount += agreement.getAmount();
        for (PowerSaleAgreement agreement: _currentSellAgreements) _nextRequiredAmount += agreement.getAmount();
    }

    // Time is expiring, make sure we have purchased enough electricity
    protected void TimePush(int ms_left) {
        if (_nextRequiredAmount > _nextPurchasedAmount) {
            LogVerbose(getName() + " requires: " + _nextRequiredAmount + " purchased: " +
                    _nextPurchasedAmount + " has " + _money + " dollars");
            sendBuyCFP(); // We need to buy more electricity
        }
        // We have enough electricity do nothing.
    }

    // We want to to buy electricity
    private void sendBuyCFP() {
        DFAgentDescription[] powerPlants = getService("powerplant");
        _currentNegotiations.clear(); // We have just received a push or new timeSlice, clear list.
        //TODO make more complicated logic for initial offer.
        PowerSaleProposal prop = new PowerSaleProposal(
                _nextRequiredAmount - _nextPurchasedAmount,1, getAID(), false);
        for (DFAgentDescription powerPlant : powerPlants) {
            // Make new negotiation for each powerPlant
            INegotiationStrategy strategy;
            try {
                strategy = makeNegotiationStrategy(prop, powerPlant.getName().getName());
            } catch (ExecutionException e) {
                return;
            }
            _currentNegotiations.add(strategy);
            prop.setBuyerAID(getAID());
            sendCFP(prop, powerPlant.getName());
        }
        LogDebug(getName() + " is sending cfp for: " + (_nextRequiredAmount - _nextPurchasedAmount) );
    }

    // Someone is negotiating with us.
    private class ProposalHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            if (_nextRequiredAmount > _nextPurchasedAmount){
                Optional<INegotiationStrategy> optional = _currentNegotiations.stream().filter(
                        (x) -> x.getOpponentName().equals(msg.getSender().getName())).findAny();
                //TODO, make sure we are getting the right negotiation chain for this message ID
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
                    strategy.addNewProposal(counterProposal, true);
                    sendCounterOffer(msg, counterProposal);
                    LogDebug(getName() + " offered to pay " + counterProposal.getCost()  +
                            " for electricity negotiating with " + msg.getSender().getName());
                }
                else {
                    // Accept the contract
                    PowerSaleAgreement agreement = (PowerSaleAgreement) offer;
                    sendAcceptProposal(msg, agreement);
                    _currentBuyAgreements.add(agreement);
                    saleMade(agreement);
                    LogVerbose(getName() + " agreed to buy " + agreement.getAmount() + " electricity until " +
                            agreement.getEndTime() + " from " + agreement.getSellerAID().getName());
                    updateContracts();
                    LogDebug(getName() + " has purchased: " + _nextPurchasedAmount + " and needs: " + _nextRequiredAmount);
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
            saleMade(agreement);
            LogDebug("Accepted a prop from: " + msg.getSender().getName() + " for " + agreement.getAmount() +
                " @ " + agreement.getCost());
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
        }
    }

    // Someone is wanting to buy electricity off us
    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            // A request for a price on electricity
            PowerSaleProposal proposed = getPowerSalePorposal(msg);
            if (_nextRequiredAmount > _nextPurchasedAmount) { // We have sold all the electricity we have purchased.
                if (_current_globals.getTimeLeft() > (GlobalValues.lengthOfTimeSlice() * 0.75)) {
                    // %75 percent of a cycle left, make an offer at increased price.
                    if (proposed.getCost() < 0) { // No cost added, make up one at +%25
                        proposed.setCost(_currentSellPrice * 1.25);
                    }
                    else {
                        if (proposed.getCost() < _currentSellPrice * 1.25) {
                            return; // There is already a price and it is to low
                        }
                    }
                }
            }
            else {
                if (proposed.getCost() < _currentSellPrice) proposed.setCost(_currentSellPrice);
                // else, leave the price alone, they have offered to pay more than we charge.
            }
            proposed.setSellerAID(getAID());
            sendProposal(msg, proposed);
            LogDebug(getName() + " sending a proposal to " + msg.getSender().getName());
            INegotiationStrategy strategy;
            try {
                strategy = makeNegotiationStrategy(proposed, msg.getSender().getName());
            } catch (ExecutionException e) {
                return;
            }
            _currentNegotiations.add(strategy);
        }
    }

    private INegotiationStrategy makeNegotiationStrategy(PowerSaleProposal offer, String opponentName)
            throws ExecutionException{
        if (_strategyParams.size() == 0) {
            LogError("No valid inputs to make negotiation strategy, using default");
            return new HoldForFirstOfferPrice(offer, opponentName, _current_globals.getTime());
        }
        try {
            return NegotiatorFactory.Factory.getNegotiationStrategy(_strategyParams, new BasicUtility(), getName(),
                    opponentName, offer, _current_globals.getTime());
        } catch (ExecutionException e) {
            String error = "Negotiator factory failed to initialize with: " ;
            for (String a : _strategyParams) { error += ("  " + a); }
            error += (" due to: " + e.getMessage());
            LogError(error);
            throw new ExecutionException(new Throwable("Not good baby"));
        }
    }

    private void saleMade(PowerSaleAgreement agg) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        addPowerSaleAgreement(msg, agg);
        msg.addReceiver(new AID("StatisticsAgent", AID.ISLOCALNAME));
        send(msg);
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
                AgentData (double buy_price, double sell_price, double current_sales, double current_purchases,
                           String name){
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
        private double _timeImperative = 0.05;
        private double _idealPrice = 0.5;
        private GlobalValues _createdTime;

        BasicUtility () {
            _createdTime = _current_globals;
        }

        @Override
        public double evaluate(PowerSaleProposal proposal) {
            double required = _nextRequiredAmount - _nextPurchasedAmount;
            double requiredUtil = (_supplyImperative / (Math.abs(required) < 0.1 ? 0.1 : required));
            double costDifference = (_idealPrice - proposal.getCost());
            double costDifferenceUtil = costDifference * _costImperative;
            double timeImperative =  Math.abs((GlobalValues.lengthOfTimeSlice() -
                    _current_globals.getTimeLeft()) * _timeImperative);
            return (requiredUtil + costDifferenceUtil + timeImperative);
        }


        @Override
        public boolean equals(IUtilityFunction utility) {
            return _createdTime == _current_globals;
        }

    }
}
