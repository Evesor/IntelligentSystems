package Agents.PowerPlants;

import Agents.Other.BaseAgent;
import Helpers.PowerSaleAgreement;
import Helpers.PowerSaleProposal;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
/******************************************************************************
 *  Use: A simple example of a power plant class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Preformatives understood:
 *       - CFP : Used to ask for a request of electricity
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content: "buy"
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *****************************************************************************/
public class PowerPlantAgent extends BaseAgent {
    private double _current_sell_price;
    private double _min_price;
    private double _max_production;
    private double _current_production;

    @Override
    protected void setup() {
        super.setup();
        _current_production = 10;
        _max_production = 100;
        _min_price = 0.4;
        _current_sell_price = 0.6;
        this.addResponseBehavior();
    }

    protected void SaleMade(ACLMessage msg) {

    }

    protected void UnhandledMessage(ACLMessage msg) {
        switch (msg.getPerformative()) {
            case ACLMessage.CFP: {
                sendQuote(msg);
            }
            case ACLMessage.ACCEPT_PROPOSAL: {
                quoteAcceptedMessage(msg);
            }
            case ACLMessage.REJECT_PROPOSAL: {
                quoteRejectedMessage(msg);
            }
        }
    }

    protected void TimeExpired (){

    }

    protected void TimeExpiringIn(int expireTimeMS) {

    }

    // Add all of our behaviors, only call once at construction.
    private void addResponseBehavior() {

    }

    private void sendQuote(ACLMessage msg) {
        // A request for a price on electricity
        PowerSaleProposal proposed;
        try{
            proposed = (PowerSaleProposal) msg.getContentObject();
        } catch (UnreadableException e){ return;}
        if (proposed.getAmount() > (_max_production - _current_production)) {
            // Cant sell that much electricity, don't bother putting a bit in.
            return;
        }
        if (proposed.getCost() < 0) {
            // No amount set, set how much we will sell that quantity for.
            proposed.setCost(_current_sell_price * proposed.getAmount());
        }
        else if (proposed.getCost() < _current_sell_price * proposed.getAmount()) {
            // To low a price, don't bother agreeing.
            return;
        }
        ACLMessage response = new ACLMessage();
        response.setInReplyTo(msg.getReplyWith());
        response.setSender(msg.getSender());
        response.setPerformative(ACLMessage.PROPOSE);
        try {
            response.setContentObject(proposed);
        } catch (java.io.IOException e) {}
        send(response);
    }

    private void quoteAcceptedMessage(ACLMessage msg) {
        // A quote we have previously made has been accepted.
        PowerSaleAgreement agreement;
        try{
            agreement = (PowerSaleAgreement) msg.getContentObject();
        } catch (UnreadableException e){ return;}
        if (agreement.getAmount() > (_max_production - _current_production)) {
            // Cant sell that much electricity, don't bother putting a bit in.
            quoteNoLongerValid(msg);
        }
        ACLMessage response = new ACLMessage();
        response.setInReplyTo("sale");
        response.setContent("sale");
        response.setSender(this.getAID());
        response.setPerformative(ACLMessage.INFORM);
        // Inform everyone about the sale.
        AMSAgentDescription[] agents = getAgentList();
        for (AMSAgentDescription agent: agents) {
            if (agent.getName() != this.getAID()) {
                msg.addReceiver(agent.getName());
            }
        }
        try {
            response.setContentObject(agreement);
        } catch (java.io.IOException e) {}
        send(response);
        _current_production += agreement.getAmount();
    }

    private void quoteRejectedMessage (ACLMessage msg) {

    }

    private void quoteNoLongerValid(ACLMessage msg) {
        // Needs implementation
    }

}
