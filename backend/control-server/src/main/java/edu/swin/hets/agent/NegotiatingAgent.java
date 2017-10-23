package edu.swin.hets.agent;

import com.hierynomus.msdtyp.ACL;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.UUID;

/******************************************************************************
 *  Use: An abstract base agent class used to deal with some routine things
 *       that any negotiating agent would do repeatedly.
 *****************************************************************************/
public abstract class NegotiatingAgent extends BaseAgent{
    @Override
    protected void setup () {
        super.setup();
    }


    ACLMessage sendSaleMade(PowerSaleAgreement agg) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        addPowerSaleAgreement(msg, agg);
        msg.addReceiver(new AID("StatisticsAgent", AID.ISLOCALNAME));
        send(msg);
        return msg;
    }

    ACLMessage sendProposal(ACLMessage origionalMSG, PowerSaleProposal prop) {
        ACLMessage response = origionalMSG.createReply();
        response.setPerformative(ACLMessage.PROPOSE);
        addPowerSaleProposal(response, prop);
        send(response);
        return response;
    }

    ACLMessage sendAcceptProposal (ACLMessage origionalMSG, PowerSaleAgreement agg) {
        ACLMessage acceptMsg = origionalMSG.createReply();
        acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addPowerSaleAgreement(acceptMsg, agg);
        send(acceptMsg);
        return acceptMsg;
    }

    ACLMessage sendCFP (PowerSaleProposal prop, AID reciver) {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        addPowerSaleProposal(cfp, prop);
        cfp.setConversationId(UUID.randomUUID().toString());
        cfp.addReceiver(reciver);
        send(cfp);
        return cfp;
    }

    ACLMessage sendRejectProposalMessage(ACLMessage origionalMsg, PowerSaleProposal prop) {
        ACLMessage response = origionalMsg.createReply();
        response.setPerformative(ACLMessage.REJECT_PROPOSAL);
        response.setSender(getAID());
        addPowerSaleProposal(response, prop);
        send(response);
        return response;
    }
    /*
    *  Note agents should not reject contracts negotiated in good faith, contracts
    *  should only be invalidated by timing events causing them to no longer be valid.
     */
    ACLMessage sendRejectAgreementMessage(ACLMessage origionalMsg, PowerSaleAgreement agg) {
        ACLMessage response = origionalMsg.createReply();
        response.setPerformative(ACLMessage.REJECT_PROPOSAL);
        response.setSender(getAID());
        addPowerSaleAgreement(response, agg);
        send(response);
        return response;
    }

    void addPowerSaleAgreement(ACLMessage msg, PowerSaleAgreement ag) {
        try {
            msg.setContentObject(ag);
        }catch (IOException e) {
            LogError("Tried to attach a power sale agreement to message, error thrown");
        }
    }

    void addPowerSaleProposal(ACLMessage msg, PowerSaleProposal prop) {
        try {
            msg.setContentObject(prop);
        }catch (IOException e) {
            LogError("Tried to attach a power sale agreement to message, error thrown");
        }
    }

    PowerSaleProposal getPowerSalePorposal(ACLMessage msg) {
        try {
            return (PowerSaleProposal)msg.getContentObject();
        }catch (UnreadableException e) {
            LogError("Tried to read a power sale agreement from message, error thrown");
            return null;
        }
    }

    PowerSaleAgreement getPowerSaleAgrement (ACLMessage msg) {
        try {
            return (PowerSaleAgreement) msg.getContentObject();
        }catch (UnreadableException e) {
            LogError("Tried to read a power sale agreement from message, error thrown");
            return null;
        }
    }

}
