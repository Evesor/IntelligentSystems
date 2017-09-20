package Agents.PowerPlants;

import Agents.Other.BaseAgent;
import Helpers.GoodMessageTemplates;
import Helpers.IMessageHandler;
import Helpers.PowerSaleAgreement;
import Helpers.PowerSaleProposal;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.List;
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
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleProposal"));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleAgreement"));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleProposal"));

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

    protected void TimeExpired (){
        // Update how much electricity we are selling.
        _current_production = 0;
        for (PowerSaleAgreement agreement: _current_contracts) {
            if (agreement.getEndTime() > _current_globals.getTime()) {
                _current_contracts.removeElement(agreement); //  Valid in JAVA? cool:D
            }
            if (agreement.getStartTime() <= _current_globals.getTime()) {
                _current_production += agreement.getAmount(); //Update current production values.
            }
        }
    }

    protected String getJSON() {
        return "Not implemented";
    }

    protected void TimePush(int ms_left) {

    }

    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            // A request for a price on electricity
            PowerSaleProposal proposed;
            try{
                proposed = (PowerSaleProposal) msg.getContentObject();
            } catch (UnreadableException e){
                sendNotUndersood(msg, "no proposal found");
                return;
            }
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
                //TODO Add negotiation here to try and make a more agreeable price.
                return;
            }
            ACLMessage response = msg.createReply();
            response.setPerformative(ACLMessage.PROPOSE);
            try {
                response.setContentObject(proposed);
            } catch (java.io.IOException e) { return; }
            send(response);
            LogVerbose(getName() + " sending a proposal to " + msg.getSender().getName());
        }
    }

    private class QuoteRejectedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            // Don't care ATM
        }
    }

    private class QuoteAcceptedHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            // A quote we have previously made has been accepted.
            PowerSaleAgreement agreement;
            try{
                agreement = (PowerSaleAgreement) msg.getContentObject();
            } catch (UnreadableException e){ return;}
            if (agreement.getAmount() > (_max_production - _current_production)) {
                // Cant sell that much electricity, send back error message.
                quoteNoLongerValid(msg);
                return;
            }
            _current_contracts.add(agreement);
            // Inform other about sale
            ACLMessage InformMessage = new ACLMessage(ACLMessage.INFORM);
            InformMessage.setSender(getAID());
            // Inform everyone about the sale.
            AMSAgentDescription[] agents = getAgentList();
            for (AMSAgentDescription agent: agents) {
                if (agent.getName() != getAID()) {
                    msg.addReceiver(agent.getName());
                }
            }
            try {
                InformMessage.setContentObject(agreement);
            } catch (java.io.IOException e) { return; }
            send(InformMessage);
            _current_production += agreement.getAmount();
        }
    }

    private void quoteNoLongerValid(ACLMessage msg) {
        //TODO : Needs implementation
    }

}
