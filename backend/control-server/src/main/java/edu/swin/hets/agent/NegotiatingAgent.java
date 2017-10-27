package edu.swin.hets.agent;

import edu.swin.hets.helper.*;
import edu.swin.hets.helper.negotiator.HoldForFirstOfferPrice;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/******************************************************************************
 *  Use: An abstract base agent class used to deal with some routine things
 *       that any negotiating agent would do repeatedly.
 *****************************************************************************/
public abstract class NegotiatingAgent extends BaseAgent{
    public static String CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY = "ChangeStrategy";
    // Used to define external message to change the negotiation strategy.
    MessageTemplate ChangeNegotiationStrategyTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchOntology(CHANGE_NEGOTIATION_STRATEGY_ONTOLOGY));
            //TODO, once we have front end messages sorted check the sender.
    @Override
    protected void setup () {
        super.setup();
    }


    ACLMessage sendSaleMade(PowerSaleAgreement agg) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        addPowerSaleAgreement(msg, agg);
        msg.addReceiver(new AID(StatisticsAgent.AGENT_NAME, AID.ISLOCALNAME));
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
    /*
    *       Wrapper around the factory to make things easier.
     */
    INegotiationStrategy makeNegotiationStrategy(PowerSaleProposal offer,
                                                         String conversationID ,
                                                         IUtilityFunction utilityFunction,
                                                         String opponentName,
                                                         int currentTime,
                                                         List<String> negotiationArgs) throws ExecutionException {
        if(negotiationArgs.size() < NegotiatorFactory.MIN_NUMBER_OF_ARGS ) {
            LogError("Does not have any details to make negotiators with, using a default");
            negotiationArgs.forEach((arg) -> LogError("was passed: " + arg));
            return new HoldForFirstOfferPrice(offer, conversationID, opponentName, _current_globals,
                    10, 8, 0.1, 0.1, 0.5);
        }
        try {
            return NegotiatorFactory.Factory.getNegotiationStrategy(negotiationArgs, utilityFunction,
                    opponentName, offer, _current_globals, conversationID);
        } catch (ExecutionException e) {
            String error = "Negotiator factory failed to initialize with: " ;
            for (String a : negotiationArgs) { error += ("  " + a); }
            error += (" due to: " + e.getMessage());
            LogError(error);
            throw new ExecutionException(new Throwable("Not good baby"));
        }
    }

}
