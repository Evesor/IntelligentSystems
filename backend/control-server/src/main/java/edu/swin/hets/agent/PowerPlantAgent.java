package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.Serializable;
import java.util.Vector;

/******************************************************************************
 *  Use: A simple example of a power plant class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Services Registered: "powerplant"
 *  Messages understood:
 *       - CFP : Used to ask for a request of electricity
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal
 *             content Object: A PowerSaleProposal object
 *   Messages Sent:
 *       - NOT-UNDERSTOOD : Used to signal that there was no attached prop obj
 *              content: "no proposal found"
 *       - PROPOSE : Used to send out a proposal to someone
 *              content Object : A power sale proposal obj
 *****************************************************************************/
public class PowerPlantAgent extends BaseAgent {
    private double _current_sell_price;
    private double _max_production;
    private double _current_production;
    private Vector<PowerSaleAgreement> _current_contracts;

    private MessageTemplate CFPMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleProposal"));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleAgreement"));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            GoodMessageTemplates.ContatinsString("edu.swin.hets.helper.PowerSaleProposal"));

    @Override
    protected void setup() {
        super.setup();
        _current_production = 10;
        _max_production = 300;
        _current_sell_price = 0.6;
        addMessageHandler(CFPMessageTemplate, new CFPHandler());
        addMessageHandler(PropAcceptedMessageTemplate, new QuoteAcceptedHandler());
        addMessageHandler(PropRejectedMessageTemplate, new QuoteRejectedHandler());
        _current_contracts = new Vector<PowerSaleAgreement>();
        RegisterAMSService(getAID().getName(),"powerplant");
    }

    // Update bookkeeping.
    protected void TimeExpired (){
        // Update how much electricity we are selling.
        _current_production = 0;
        Vector<PowerSaleAgreement> toRemove = new Vector<>();
        for (PowerSaleAgreement agreement: _current_contracts) {
            if (agreement.getEndTime() < _current_globals.getTime()) {
                toRemove.add(agreement);
            }
        }
        _current_contracts.removeAll(toRemove);
        for (PowerSaleAgreement agreement: _current_contracts) {
            if (agreement.getStartTime() <= _current_globals.getTime()) {
                _current_production += agreement.getAmount(); //Update current production values.
            }
        }
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

    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            // A request for a price on electricity
            PowerSaleProposal proposed = getPowerSalePorposal(msg);
            if (proposed.getAmount() > (_max_production - _current_production)) {
                // Cant sell that much electricity, don't bother putting a bit in.
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
            send(response);
            LogVerbose(getName() + " sending a proposal to " + msg.getSender().getName());
        }
    }

    private class QuoteRejectedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            // Don't care ATM
            //TODO add a rect message
        }
    }

    private class QuoteAcceptedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            // A quote we have previously made has been accepted.
            PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
            if (agreement.getAmount() > (_max_production - _current_production)) {
                // Cant sell that much electricity, send back error message.
                quoteNoLongerValid(msg);
                return;
            }
            _current_contracts.add(agreement);
        }
    }

    private void quoteNoLongerValid(ACLMessage msg) {
        //TODO : Needs implementation
        sendRejectProposalMessage(msg);
    }

    private class PowerPlantData implements Serializable{
        private String Name;
        private double current_sell_price;
        private double current_production;
        public PowerPlantData(double sell_price, double production, String name) {
            current_sell_price = sell_price;
            current_production = production;
            Name = name;
        }
        public String getName() { return Name; }
        public double getCurrent_production() { return current_production; }
        public double getCurrent_sell_price() { return current_sell_price; }
    }
}