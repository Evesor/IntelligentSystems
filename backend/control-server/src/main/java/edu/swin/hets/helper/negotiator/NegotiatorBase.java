package edu.swin.hets.helper.negotiator;


import edu.swin.hets.helper.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

/******************************************************************************
 *  Use: A base class for providing useful functions for Negotiators
 *  Notes:
 *      Max negotiation time should be more than max stall time.
 *****************************************************************************/
public abstract class NegotiatorBase implements INegotiationStrategy{
    private ArrayList<PowerSaleProposal[]> _proposalHistory; // Will always be a 2 wide array
    private String _opponentName;
    private String _conversationID;
    private int _maxStallTime;
    private int _maxNegotiationTime;
    GlobalValues _currentGlobals;

    NegotiatorBase(String opponentName, String conversationID, PowerSaleProposal firstOffer,
                   int maxNegotiationTime, int maxStallTime, GlobalValues currentGlobals) {
        _conversationID = conversationID;
        _opponentName = opponentName;
        _proposalHistory = new ArrayList<>();
        _proposalHistory.add(new PowerSaleProposal[2]);
        _proposalHistory.get(0)[0] = firstOffer;
        _maxNegotiationTime = maxNegotiationTime;
        _maxStallTime = maxStallTime;
        _currentGlobals = currentGlobals;
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

    @Override
    public String getOpponentName() {
        return _opponentName;
    }

    @Override
    public String getConversationID() {
        return _conversationID;
    }

    boolean areWeSeller() {
        return  getOurMostRecentOffer().getBuyerAID().getName().equals(_opponentName);
    }
    /*
    *   Figure out if we are just in a constant loop, look for the same props in last n operations.
     */
    private boolean sameAsLastNOffers (PowerSaleProposal prop, int n) {
        if (getNumberOfProps() < n + 1) return false; // We haven't got n offers yet, keep waiting.
        boolean oneDifferent = false;
        int lastIndex = getNumberOfProps() - 1;
        int firstIndex = getNumberOfProps() - n;
        for (int i = firstIndex; i < lastIndex; i++) {
            if (getProposal(i) == null) return true;
            if (!prop.equalValues(getProposal(i))) oneDifferent = true;
        }
        return !oneDifferent;
    }

    boolean conversationStalled () {
        return sameAsLastNOffers(getThereMostRecentOffer(), _maxStallTime);
    }

    boolean conversationToLong () {
        return getNumberOfProps() > _maxNegotiationTime;
    }

    private int getNumberOfProps () { return _proposalHistory.size(); }

    PowerSaleProposal getOurMostRecentOffer () {
        for (int i = _proposalHistory.size() - 1; i >= 0; i--) {
            if (_proposalHistory.get(i)[0] != null) {
                return _proposalHistory.get(i)[0];
            }
        }
        return null; //Should not be possible.
    }

    @Nullable
    PowerSaleProposal getProposal (int index) {
        if (index < _proposalHistory.size() && index >= 0) return _proposalHistory.get(index)[0];
        return null;
    }

    @Nullable
    PowerSaleProposal getThereMostRecentOffer () {
        for (int i = _proposalHistory.size() - 1; i >= 0; i--) {
            if (_proposalHistory.get(i) != null) return _proposalHistory.get(i)[1];
        }
        return null;
    }
    /*
    *   Recursive function that looks for an offer that is closer to the one offered that is still acceptable.
    *   Note : acceptanceLimit is a percentage DO NOT EXCEEDED 0<->1
     */
    //TODO, Deal with buy and sell logic
    Optional<IPowerSaleContract> linearMove(PowerSaleProposal ourProposal,
                                                    PowerSaleProposal thereProposal,
                                                    IUtilityFunction utility,
                                                    double volumeJump,
                                                    int timeJump,
                                                    double priceJump,
                                                    double acceptanceLimit) {
        PowerSaleProposal counter = new PowerSaleProposal(
                changeVolume(thereProposal, ourProposal, volumeJump),
                changeTime(thereProposal, ourProposal, timeJump),
                changeCost(thereProposal, ourProposal, priceJump),
                ourProposal.getSellerAID(),
                ourProposal.getBuyerAID());
        if (utility.evaluate(ourProposal) * acceptanceLimit < utility.evaluate(counter)) {
            // This counter is pretty good, return it.
            return Optional.of(counter);
        }
        // We have just evaluated the quality of a deal near what the other person wanted and it wasn't good enough.
        if (withinStepSize(counter, thereProposal, timeJump, volumeJump, priceJump)) return Optional.empty();
        // Recursively call till we get to there offer or we find an acceptable one.
        return linearMove(counter, thereProposal, utility, volumeJump, timeJump, priceJump, acceptanceLimit);
    }

    private double changeCost(PowerSaleProposal thereProp, PowerSaleProposal ourProp, double change) {
        if (thereProp.getCost() - ourProp.getCost() < change) return thereProp.getCost();
        else if ((thereProp.getCost() - ourProp.getCost()) > 0) {// We need to increase
            return ourProp.getCost() + change;
        }
        else {// We need to decrease
            return ourProp.getCost() - change;
        }
    }
    private double changeVolume(PowerSaleProposal thereProp, PowerSaleProposal ourProp, double change) {
        if (thereProp.getAmount() - ourProp.getAmount() < change) return thereProp.getAmount();
        else if ((thereProp.getAmount() - ourProp.getAmount()) > 0) {// We need to increase
            return ourProp.getAmount() + change;
        }
        else {// We need to decrease
            return ourProp.getAmount() - change;
        }
    }
    private int changeTime(PowerSaleProposal thereProp, PowerSaleProposal ourProp, int change) {
        if (thereProp.getDuration() - ourProp.getDuration() < change) return thereProp.getDuration();
        else if ((thereProp.getDuration() - ourProp.getDuration()) > 0) {// We need to increase
            return ourProp.getDuration() + change;
        }
        else {// We need to decrease
            return ourProp.getDuration() - change;
        }
    }

    private boolean withinStepSize (PowerSaleProposal p1, PowerSaleProposal p2, int timeJump,
                                    double volumeJump, double priceJump) {
        return (Math.abs((p1.getAmount() - p2.getAmount())) < volumeJump &&
                Math.abs((p1.getCost() - p2.getCost())) < priceJump &&
                Math.abs((p1.getDuration() - p2.getDuration())) < timeJump);
    }
}
