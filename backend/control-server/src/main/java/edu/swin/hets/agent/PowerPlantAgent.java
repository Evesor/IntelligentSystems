package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
/******************************************************************************
 *  Use: A simple example of a power plant class that is not dependant
 *       IsOn any events, should be extended later for more detailed classes.
 *  Services Registered: "powerplant"
 *  Messages understood:
 *       - CFP : Used to ask for a request of electricity
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal, can also be used
 *                           invalidate an agreement that came back to late
 *             content Object: A PowerSaleProposal object
 *       - REQUEST : Used to ask agent to change its negotiation mechanism
 *             content Object: List<String>, arguments to change the strategy
 *   Messages Sent:
 *       - NOT-UNDERSTOOD : Used to signal that there was no attached prop obj
 *              content: "no proposal found"
 *       - PROPOSE : Used to send out a proposal to someone
 *              content Object : A power sale proposal obj
 *****************************************************************************/
public class PowerPlantAgent extends NegotiatingAgent {
    private static final int GROUP_ID = 1;
    private static String TYPE = "Power Plant";
    public static double BASE_COST = 5;
    private static double MAX_PRODUCTION;
    private double _money;
    private double _costOfProduction;
    private double _currentIdealSellPrice;
    private double _currentProduction;
    private ArrayList<PowerSaleAgreement> _currentContracts;
    private ArrayList<INegotiationStrategy> _currentNegotiations;
    private List<String> _negotiationArgs;

    private MessageTemplate CFPMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));
    private MessageTemplate ProposeTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            MessageTemplate.or(GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()),
                    GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName())));

    @Override
    protected void setup() {
        super.setup();
        _money = 500;
        _currentProduction = 10;
        MAX_PRODUCTION = 1000;
        _costOfProduction = 0.5;
        _currentIdealSellPrice = 1.8;
        _currentContracts = new ArrayList<>();
        _currentNegotiations = new ArrayList<>();
        RegisterAMSService(getAID().getName(),"powerplant");
        addMessageHandler(CFPMessageTemplate, new CFPHandler());
        addMessageHandler(PropAcceptedMessageTemplate, new QuoteAcceptedHandler());
        addMessageHandler(PropRejectedMessageTemplate, new QuoteRejectedHandler());
        addMessageHandler(ProposeTemplate, new ProposeHandler());
        addMessageHandler(ChangeNegotiationStrategyTemplate, new ChangeNegotiationStrategyHandler());
        _negotiationArgs = (List<String>) getArguments()[0];
//        if (_negotiationArgs.size() > 0) {
//            _negotiationArgs.forEach((arg) -> LogDebug("was passed: " + arg));
//        }
    }

    // Update bookkeeping.
    protected void TimeExpired (){
        updateContracts();
        balanceBooks();
        _currentNegotiations.clear();
        LogVerbose(getName() + " is producing: " + _currentProduction);
    }

    protected String getJSON() {
        String json = "";
        try {
            json = new ObjectMapper().writeValueAsString(
                    new PowerPlantData(_currentIdealSellPrice, _currentProduction, getName()));
        }
        catch (JsonProcessingException e) {
            LogError("Error parsing data to json in " + getName() + " exeption thrown");
        }
        return json;
    }

    protected void TimePush(int ms_left) {

    }

    private void balanceBooks () {
        calculateProductionCost();
        _currentContracts.forEach((agg) -> _money += agg.getCost() * agg.getAmount());
        _currentContracts.forEach((agg) -> _money -= agg.getAmount() * _costOfProduction);
    }

    private void updateContracts() {
        _currentProduction = 0;
        ArrayList<PowerSaleAgreement> toRemove = new ArrayList<>();
        // Filter out old contracts
        _currentContracts.stream().filter(
                (agg) -> agg.getEndTime() <= _current_globals.getTime()).forEach(toRemove::add);
        _currentContracts.removeAll(toRemove);
        // Update how much we now need to produce.
        _currentContracts.forEach((agg) -> _currentProduction += agg.getAmount());
    }

    private void calculateProductionCost() {
        // Normalize value production over ratio of 0->PI => O and use Base*(1-0.5*sin(O)) to make cost.
        if (_costOfProduction == 0) _costOfProduction = BASE_COST;
        _costOfProduction = BASE_COST *(1 - 0.5 * Math.sin((_currentProduction / MAX_PRODUCTION) * Math.PI));
    }

    // Someone buying from us.
    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            // A request for a price IsOn electricity
            PowerSaleProposal proposed = getPowerSalePorposal(msg);
            if (proposed.getAmount() > (MAX_PRODUCTION - _currentProduction)) {
                LogVerbose(getName() + " was asked to sell electricity than it can make.");
                return;
            }
            else if (proposed.getCost() < _currentIdealSellPrice) proposed.setCost(_currentIdealSellPrice);
            ACLMessage sent = sendProposal(msg, proposed);
            INegotiationStrategy strategy;
            try {
                strategy = makeNegotiationStrategy(proposed, sent.getConversationId(), new BasicUtility(),
                        msg.getSender().getName(), _current_globals.getTime(), _negotiationArgs);
            } catch (ExecutionException e) {
                return;
            }
            _currentNegotiations.add(strategy);
            LogVerbose(getName() + " sending a proposal for " +  proposed.getAmount() + " @ " +
                    proposed.getCost() + " to: "  + msg.getSender().getName());
        }
    }

    // Someone has rejected a quote
    private class QuoteRejectedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            if (GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()).match(msg)) {
                // Someone is rejecting a contract, remove it.
                PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
                ArrayList<PowerSaleAgreement> toRemove = new ArrayList<>();
                _currentContracts.stream().filter((agg) -> agg.equalValues(agreement)).forEach(toRemove::add);
                _currentContracts.removeAll(toRemove);
            }
            if (GoodMessageTemplates.ContatinsString(PowerSaleProposal.class.getName()).match(msg)) {
                ArrayList<INegotiationStrategy> toRemove = new ArrayList<>();
                 _currentNegotiations.stream().filter(
                         (prop) -> prop.getConversationID().equals(msg.getConversationId())).
                         forEach((prop) -> toRemove.add(prop));
                 _currentNegotiations.removeAll(toRemove);
            }
        }
    }

    // Someone agreeing to buy electricity from us.
    private class QuoteAcceptedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            // A quote we have previously made has been accepted.
            PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
            if (agreement.getAmount() > (MAX_PRODUCTION - _currentProduction)) {
                // Cant sell that much electricity, send back error message.
                sendRejectAgreementMessage(msg, agreement);
                return;
            }
            _currentContracts.add(agreement);
        }
    }

    // Someone is being difficult and haggling.... Sigh
    private class ProposeHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            Optional<INegotiationStrategy> opt = _currentNegotiations.stream().filter(
                    (neg) -> neg.getOpponentName().equals(msg.getSender().getName())).findAny();
            if (!opt.isPresent()) {
                LogError(getName() + " got a proposal from someone it was not negotiating with.");
                return;
            }
            INegotiationStrategy strategy = opt.get();
            PowerSaleProposal prop = getPowerSalePorposal(msg);
            strategy.addNewProposal(prop, false);
            Optional<IPowerSaleContract> response = strategy.getResponse();
            if (!response.isPresent()) { // We should end negotiations.
                //LogDebug("has stopped negotiating with: " + msg.getSender());
                _currentNegotiations.remove(strategy);
                sendRejectProposalMessage(msg, prop);
                return;
            }
            if (response.get() instanceof PowerSaleProposal) {
                // Make counter offer
                PowerSaleProposal counter = (PowerSaleProposal) response.get();
                sendProposal(msg, counter);
                strategy.addNewProposal(counter, true);
//                LogDebug(getName() + " offered to pay " + counter.getCost()  +
//                        " for electricity negotiating with " + msg.getSender().getName());
            }
            else { // Accept
                PowerSaleAgreement agreement = (PowerSaleAgreement) response.get();
                sendAcceptProposal(msg, agreement);
                _currentContracts.add(agreement);
                updateContracts();
                LogVerbose(getName() + " has just agreed to sell " + agreement.getAmount() + " from " + agreement);
            }
        }
    }

    private class ChangeNegotiationStrategyHandler implements IMessageHandler {
        @Override
        public void Handler(ACLMessage msg) {
            String [] arguments = msg.getContent().split(" ");
            if (arguments.length > 0) {
                _negotiationArgs = Arrays.asList(arguments);
                LogDebug("Had its strategy changed to: ");
                _negotiationArgs.forEach((arg) -> LogDebug(arg));
            }
            else LogError("tried to have its negation strategy changed to nothing");
        }
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
        public AgentData getagentData() { return dat; }
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
            public String getName () { return getLocalName(); }
        }
    }
    /******************************************************************************
     *  Use: Used to define how useful a deal is to us.
     *****************************************************************************/
    private class BasicUtility implements IUtilityFunction{

        @Override
        public double evaluate(PowerSaleProposal proposal) {
            return 0;
        }

        @Override
        public boolean equals(IUtilityFunction utility) {
            return false;
        }
    }
}