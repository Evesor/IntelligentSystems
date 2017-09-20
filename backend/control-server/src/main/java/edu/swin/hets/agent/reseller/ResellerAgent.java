package edu.swin.hets.agent.reseller;

import edu.swin.hets.agent.other.BaseAgent;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Vector;

/******************************************************************************
 *  Use: A simple example of a reseller agent class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Messages Understood:
 *       - CFP : Used to negotiate purchasing electricity from reseller.
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content: "buy"
 *             content Object: A PowerSaleAgreement object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content: "sell"
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *  Messages sent:
 *       - CFP : Used to negotiate purchasing electricity from power plant agent
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content: "buy"
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *****************************************************************************/
public class ResellerAgent extends BaseAgent {
    private double _current_sell_price;
    private double _current_by_price;
    private Vector<PowerSaleAgreement> _current_agrements;
    private Vector<PowerSaleProposal> _awaitingProposals;

    private MessageTemplate CFPMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            GoodMessageTemplates.ContatinsString("buy"));
    private MessageTemplate PropAcceptedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            MessageTemplate.MatchContent("buy"));
    private MessageTemplate PropRejectedMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
            MessageTemplate.MatchContent("buy"));

    protected void setup() {
        super.setup();
        _awaitingProposals = new Vector<PowerSaleProposal>();
        _current_agrements = new Vector<PowerSaleAgreement>();
        addMessageHandler(PropAcceptedMessageTemplate, new ProposalAcceptedHandler());
        addMessageHandler(PropRejectedMessageTemplate, new ProposalRejectedHandler());
        addMessageHandler(CFPMessageTemplate, new CFPHandler());
    }

    protected void TimeExpired() {

    }

    private class ProposalAcceptedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            msg.getContent();
            // DO what you want
        }
    }

    private class ProposalRejectedHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {
            msg.getContent();
            // DO what you want
        }
    }

    private class CFPHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {

            msg.getContent();
            // DO what you want
        }
    }
}
