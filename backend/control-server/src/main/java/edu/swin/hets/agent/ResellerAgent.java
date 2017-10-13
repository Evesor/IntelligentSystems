package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.*;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
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
    private double _current_sell_price;
    private double _current_by_price;
    private double _min_purchase_amount;
    private double _next_purchased_amount;
    private double _next_required_amount;
    private Vector<Double> _future_needs;
    private Vector<PowerSaleAgreement> _current_buy_agrements;
    private Vector<PowerSaleAgreement> _current_sell_agrements;
    private HashMap<AID, ArrayList<PowerSaleAgreement>> _customerDB;
    private HashMap<AID, ArrayList<PowerSaleAgreement>> _sellerDB;
    private ArrayList<NegotiationChain> _currentNegotiations;


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
        _current_buy_agrements = new Vector<PowerSaleAgreement>();
        _current_sell_agrements = new Vector<PowerSaleAgreement>();
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
        LogDebug(getName() + " is making json");
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
        updateContracts();
        // We now know how much we have bought and how much we need to buy
        // Start making CFP's to get electricity we need.
        if (_next_required_amount > _next_purchased_amount) {
            sendBuyCFP();
        }
    }

    private void updateContracts() {
        _next_purchased_amount = 0;
        Vector<PowerSaleAgreement> toRemove = new Vector<>();
        for (PowerSaleAgreement agreement : _current_buy_agrements) {
            if (agreement.getEndTime() < _current_globals.getTime()) {
                // No longer valid
                toRemove.add(agreement);
            }
        }
        _current_buy_agrements.removeAll(toRemove);
        for (PowerSaleAgreement agreement : _current_buy_agrements) {
            // We have purchased this electricty.
            _next_purchased_amount += agreement.getAmount();
        }
        _next_required_amount = 0;
        for (PowerSaleAgreement agreement: _current_sell_agrements) {
            if (agreement.getEndTime() >= _current_globals.getTime()) {
                toRemove.add(agreement);// No longer valid
            }
        }
        _current_sell_agrements.removeAll(toRemove);
        for (PowerSaleAgreement agreement: _current_sell_agrements) {
            _next_required_amount += agreement.getAmount();
        }
    }

    // Time is expiring, make sure we have purchased enough electricity
    protected void TimePush(int ms_left) {
        if (_next_required_amount > _next_purchased_amount ) {
            LogVerbose(getName() + "requires: " + _next_required_amount + " purchased: " + _next_purchased_amount);
            sendBuyCFP(); // We need to buy more electricity
        }
        // We have enough electricity do nothing.
    }

    // We want to to buy electricity
    private void sendBuyCFP() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        DFAgentDescription[] powerplants = getService("powerplant");
        for (DFAgentDescription powerplant : powerplants) {
            cfp.addReceiver(powerplant.getName()); //CFP to each power plant
        }
        //TODO make more complicated logic.
        PowerSaleProposal prop = new PowerSaleProposal(
                _next_required_amount - _next_purchased_amount,1, getAID(), false);
        prop.setBuyerAID(getAID());
        addPowerSaleProposal(cfp, prop);
        cfp.setSender(getAID());
        send(cfp);
    }

    // Someone is offering to sell us electricity
    private class ProposalHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            PowerSaleProposal proposed = getPowerSalePorposal(msg);
            if (proposed.getCost() <= _current_by_price ) {
                // Accept
                LogVerbose(getName() + " agreed to buy " + proposed.getAmount() + " electricity for " +
                        proposed.getDuration() + " time slots from " + proposed.getSellerAID().getName());
                PowerSaleAgreement contract = new PowerSaleAgreement(proposed, _current_globals.getTime());
                _current_buy_agrements.add(contract);
                updateContracts();
                ACLMessage acceptMsg = msg.createReply();
                acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                addPowerSaleAgreement(acceptMsg, contract);
                acceptMsg.setSender(getAID());
                send(acceptMsg);
            } else {
                // To expensive
                sendRejectProposalMessage(msg);
            }
        }
    }

    // Someone is buying electricity off us
    private class ProposalAcceptedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
            _current_sell_agrements.add(agreement);
        }
    }

    // Someone has rejected a quote
    private class ProposalRejectedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            if (GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleAgreement").match(msg)) {
                // Someone is rejecting a contract, remove it.
                PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
                PowerSaleAgreement toRemove = null;
                // Try and find agreement.
                for (PowerSaleAgreement agg : _current_buy_agrements) {
                    if (agg.equalValues(agreement)) {
                        toRemove = agg;
                        break;
                    }
                }
                if (toRemove != null) {
                    _current_buy_agrements.remove(toRemove);
                }
                toRemove = null;
                // Try and find agreement.
                for (PowerSaleAgreement agg : _current_sell_agrements) {
                    if (agg.equalValues(agreement)) {
                        toRemove = agg;
                        break;
                    }
                }
                if (toRemove != null) {
                    _current_sell_agrements.remove(toRemove);
                }
            }
            if (GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleProposal").match(msg)) {
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

    private void nogotiatePrice () {

    }

    private void quoteNoLongerValid(ACLMessage msg) {

    }
    /******************************************************************************
     *  Use: Used by getJson to output data to server.
     *****************************************************************************/
    private class ResellerAgentData implements Serializable {
        private String Name;
        private double current_sell_price;
        private double current_buy_price;
        private double current_sales_volume;
        private double current_purchase_volume;
        private Integer groupNumber = 2;
        ResellerAgentData(double buy_price, double sell_price, double current_sales, double current_purchases, String name) {
            current_sell_price = sell_price;
            current_buy_price = buy_price;
            current_sales_volume = current_sales;
            current_purchase_volume = current_purchases;
            Name = name;
        }
        public String getName() { return Name; }
        public String gettype () { return TYPE; }
        public double getCurrent_sell_price() { return current_sell_price; }
        public double getCurrent_buy_price() { return current_buy_price; }
        public double getCurrent_purchase_volume() { return current_purchase_volume; }
        public double getCurrent_sales_volume() { return current_sales_volume; }
        public int getgroup() { return GROUP_ID; }
    }

    /******************************************************************************
     *  Use: An object that is used to deal with the logic of negotiating with a
     *       potential customer or supplier.
     *****************************************************************************/
    private class NegotiationChain {
        private ArrayList<PowerSaleAgreement> _previosAgrements;
        private double _base;
        private double _aggressiveness;
        private double _time_imperitive;
        private double _wastage_tolorance;

        NegotiationChain() {

        }

        public void addResponse (ACLMessage msg) {

        }

        public void getConversationID () {

        }

    }
}
