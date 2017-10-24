package edu.swin.hets.helper.negotiator;


import edu.swin.hets.helper.INegotiationStrategy;
import edu.swin.hets.helper.PowerSaleProposal;
import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class NegotiatorBase implements INegotiationStrategy{
    private ArrayList<PowerSaleProposal[]> _proposalHistory; // Will always be a 2 wide array
    private String _opponentName;
    private String _conversationID;

    NegotiatorBase(String opponentName, String conversationID, PowerSaleProposal firstOffer) {
        _conversationID = conversationID;
        _opponentName = opponentName;
        _proposalHistory = new ArrayList<>();
        _proposalHistory.add(new PowerSaleProposal[2]);
        _proposalHistory.get(0)[0] = firstOffer;
    }

    private NegotiatorBase() {} // Not valid

    @Override
    public void addNewProposal(PowerSaleProposal proposal, boolean fromUs) {
        if (fromUs) {
            _proposalHistory.add(new PowerSaleProposal[2]);
            _proposalHistory.get(_proposalHistory.size() - 1)[0] = proposal;
        }
        else if (_proposalHistory.get(_proposalHistory.size() - 1) == null){
            _proposalHistory.get(_proposalHistory.size() - 1)[1] = proposal;
        }
        else {
            _proposalHistory.add(new PowerSaleProposal[2]);
            _proposalHistory.get(_proposalHistory.size() - 1)[1] = proposal;
        }
    }

    boolean areWeSeller() {
        if (getOurMostRecentOffer().getSellerAID() != null)
            return !getOurMostRecentOffer().getSellerAID().equals(_opponentName);
        if (getOurMostRecentOffer().getBuyerAID() != null)
            return !getOurMostRecentOffer().getBuyerAID().equals(_opponentName);
        return false; //Should not get called, maybe throw something?
    }

    int getNumberOfProps () { return _proposalHistory.size(); }

    @Nullable
    PowerSaleProposal getProposal (int index) {
        if (index < _proposalHistory.size() && index >= 0) return _proposalHistory.get(index)[0];
        return null;
    }

    @Override
    public String getOpponentName() {
        return _opponentName;
    }

    @Override
    public String getConversationID() {
        return _conversationID;
    }

    @Nullable
    PowerSaleProposal getOurMostRecentOffer () {
        for (int i = _proposalHistory.size() - 1; i >= 0; i--) {
            if (_proposalHistory.get(i)[0] != null) {
                return _proposalHistory.get(i)[0];
            }
        }
        return null;
    }

    @Nullable
    PowerSaleProposal getThereMostRecentOffer () {
        for (int i = _proposalHistory.size() - 1; i >= 0; i--) {
            if (_proposalHistory.get(i) != null) return _proposalHistory.get(i)[1];
        }
        return null;
    }
}
