package Agents.Resellers;

import Agents.Other.BaseAgent;
import Helpers.GoodMessageTemplates;
import Helpers.IMessageHandler;
import Helpers.PowerSaleAgreement;
import Helpers.PowerSaleProposal;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
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
    private double _current_sell_price;
    private double _current_by_price;
    private double _min_purchase_amount;
    private double _next_purchased_amount;
    private double _next_required_amount;
    private Vector<Double> _future_needs;
    private Vector<PowerSaleAgreement> _current_buy_agrements;
    private Vector<PowerSaleAgreement> _current_sell_agrements;
    private Vector<PowerSaleProposal> _awaitingProposals;

    private MessageTemplate CFPMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleProposal"));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleAgreement"));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleProposal"));
    private MessageTemplate PropMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            GoodMessageTemplates.ContatinsString("Helpers.PowerSaleProposal"));

    protected void setup () {
        super.setup();
        _current_by_price = 0.7;
        _current_sell_price = 1.0;
        _min_purchase_amount = 100;
        _next_required_amount = 200; //TODO let home users set demand.
        _awaitingProposals = new Vector<PowerSaleProposal>();
        _current_buy_agrements = new Vector<PowerSaleAgreement>();
        _current_sell_agrements = new Vector<PowerSaleAgreement>();
        addMessageHandler(PropAcceptedMessageTemplate, new ProposalAcceptedHandler());
        addMessageHandler(PropRejectedMessageTemplate, new ProposalRejectedHandler());
        addMessageHandler(CFPMessageTemplate, new CFPHandler());
        addMessageHandler(PropMessageTemplate, new ProposalHandler());
        RegisterAMSService("reseller");
    }

    protected void TimeExpired() {
        _next_purchased_amount = 0;
        for (PowerSaleAgreement agreement: _current_sell_agrements) {
            if (agreement.getEndTime() >= _current_globals.getTime()) {
                _current_sell_agrements.removeElement(agreement);
            }
            _next_required_amount += agreement.getAmount();
        }
        /*
        _next_required_amount = 0;
        for (PowerSaleAgreement agreement: _current_buy_agrements) {
            if (agreement.getEndTime() >= _current_globals.getTime()) {
                _current_buy_agrements.removeElement(agreement);
            }
            _next_purchased_amount += agreement.getAmount();
        } */ //TODO Uncomment when we are receiving orders from home users
        // We now know how much we have bought and how much we need to buy
        // Start making CFP's to get electricity we need.
        sendCFP();
    }

    protected void TimePush(int ms_left) {
        System.out.println("Reqired amount: " + _next_required_amount + " purchased amount: " + _next_purchased_amount);
        if (_next_required_amount - _next_purchased_amount > 0.1) {
            sendCFP(); // We need to buy more electricity
        }
        // We have enough electricity do nothing.
    }

    private void sendCFP() {

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        DFAgentDescription[] powerplants = getService("powerplant");
        for (DFAgentDescription powerplant: powerplants) {
            cfp.addReceiver(powerplant.getName()); //CFP to each power plant
        }
        PowerSaleProposal prop = new PowerSaleProposal(
                _next_required_amount - _next_purchased_amount,
                4); //TODO make more complicated logic.
        prop.setBuyerAID(getAID());
        try {
            cfp.setContentObject(prop);
        } catch (IOException e) {
            e.printStackTrace(); //TODO Log and deal with
        }
        send(cfp);
    }


    // Someone is offering to sell us electricity
    private class ProposalHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {

        }
    }

    // Someone is buying electricity off us
    private class ProposalAcceptedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {

        }
    }

    // Someone is not buying electricity off us
    private class ProposalRejectedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
        }
    }

    // Someone is wanting to buy electricity off us
    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
        }
    }
}
