package Agents.Resellers;

import Agents.Other.BaseAgent;
import Helpers.PowerSaleProposal;
import jade.lang.acl.ACLMessage;

import java.util.Vector;

/******************************************************************************
 *  Use: A simple example of a reseller agent class that is not dependant
 *       on any events, should be extended later for more detailed classes.
 *  Preformatives Understood:
 *       - CFP : Used to negotiate purchasing electricity from reseller.
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *       - ACCEPT_PROPOSAL : Used to signify a proposal has been accepted.
 *             content: "buy"
 *             content Object: A PowerSaleAgreement object
 *       - REJECT_PROPOSAL : Used to signify failed proposal
 *             content: "buy"
 *             content Object: A PowerSaleProposal object
 *  Preformatives used:
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
    private Vector<PowerSaleProposal> _awaitingProposals;

    protected void setup () {
        super.setup();
        _awaitingProposals = new Vector<PowerSaleProposal>();
    }

    protected void SaleMade(ACLMessage msg) {

    }

    protected void TimeExpired() {

    }

    protected void TimeExpiringIn(int expireTimeMS) {

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

    private void sendQuote(ACLMessage msg) {

    }

    private void quoteAcceptedMessage(ACLMessage msg) {

    }

    private void quoteRejectedMessage(ACLMessage msg) {

    }
}
