package Agents.PowerPlants;

import Helpers.PowerSaleAgreement;
import Helpers.PowerSaleProposial;
import Helpers.Time;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.Vector;
/******************************************************************************
 *  Use: A simple example of a power plant class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Preformatives used:
 *       - CFP : Used to ask for a request of electricity
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *****************************************************************************/
public class PowerPlantAgent extends Agent{
    private float _current_sell_price;
    private float _min_price;
    private float _max_production;
    private float _current_production;

    @Override
    protected void setup() {
        this.addResponseBehavior();
    }

    // Add all of our behaviors, only call once at construction.
    private void addResponseBehavior() {
        // Deal with any requests for quotes
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    switch (msg.getPerformative()){
                        case ACLMessage.CFP: {
                            sendQuote(msg);
                        }
                        case ACLMessage.ACCEPT_PROPOSAL:{
                            quoteAccepted(msg);
                        }
                        case ACLMessage.REJECT_PROPOSAL: {
                            quoteRejected(msg);
                        }
                    }
                }
                block();
            }
        });
    }

    private void sendQuote(ACLMessage msg) {
        // A request for a price on electricity
        PowerSaleProposial proposed;
        try{
            proposed = (PowerSaleProposial) msg.getContentObject();
        } catch (UnreadableException e){ return;}
        if (proposed.getAmount() > (_max_production - _current_production)) {
            // Cant sell that much electricity, don't bother putting a bit in.
            return;
        }
        proposed.setCost(_current_sell_price * proposed.getAmount());
        ACLMessage response = new ACLMessage();
        response.setInReplyTo(msg.getReplyWith());
        response.setSender(msg.getSender());
        response.setPerformative(ACLMessage.PROPOSE);
        try {
            response.setContentObject(proposed);
        } catch (java.io.IOException e) {}
        send(response);
    }

    private void quoteAccepted(ACLMessage msg) {
        // A quote we have previously made has been accepted.
        PowerSaleProposial quote;
        try{
            quote = (PowerSaleProposial) msg.getContentObject();
        } catch (UnreadableException e){ return;}
        if (quote.getAmount() > (_max_production - _current_production)) {
            // Cant sell that much electricity, don't bother putting a bit in.
            quoteNoLongerValid(msg);
        }
        PowerSaleAgreement agreement = new PowerSaleAgreement();
        ACLMessage response = new ACLMessage();
        response.setInReplyTo(msg.getReplyWith());
        response.setSender(msg.getSender());
        response.setPerformative(ACLMessage.PROPOSE);
        try {
            response.setContentObject(proposed);
        } catch (java.io.IOException e) {}
        send(response);
    }

    private void quoteRejected(ACLMessage msg) {

    }

    private void quoteNoLongerValid(ACLMessage msg) {

    }

}
