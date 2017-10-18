package edu.swin.hets.helper;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
/******************************************************************************
 *  Use: A basic implementation of negotiating to test interface.
 *  Notes: Repeated calls to BasicNegotiator without adding new offers will
 *         lead to the same response comng back each time.
 *
 *****************************************************************************/
public class BasicNegotiator implements INegotiationStrategy {
    private ArrayList<PowerSaleProposal[]> _proposalHistory; // Will always be a 2 wide array
    private IUtilityFunction _utilityFun;
    private String _ourName;
    private Integer _timeSlice;
    private String _opponentName;

    public BasicNegotiator (IUtilityFunction utilityFun,
                            String ourName,
                            String opponentName,
                            PowerSaleProposal firstOffer,
                            Integer timeSlice) {
        _utilityFun = utilityFun;
        _proposalHistory = new ArrayList<>();
        _proposalHistory.add(new PowerSaleProposal[2]);
        _proposalHistory.get(0)[0] = firstOffer;
        _ourName = ourName;
        _timeSlice = timeSlice;
        _opponentName = opponentName;
    }

    public String getOpponentName() { return _opponentName; }

    @Override
    public void changeUtilityFunction(IUtilityFunction utilFun) {
        _utilityFun = utilFun;
    }

    @Override
    public void addNewProposal(PowerSaleProposal proposal, boolean fromUs) {
        if (fromUs) {
            _proposalHistory.add(new PowerSaleProposal[2]);
            _proposalHistory.get(_proposalHistory.size() - 1)[0] = proposal;
        }
        else {
            _proposalHistory.get(_proposalHistory.size() - 1)[1] = proposal;
        }
    }

    @Override
    public IPowerSaleContract getResponse() {
        PowerSaleProposal ourOffer = _proposalHistory.get(_proposalHistory.size() - 1)[0];
        PowerSaleProposal thereOffer = _proposalHistory.get(_proposalHistory.size() - 1)[1];
        if (thereOffer == null && _proposalHistory.size() > 1) {
            // They have not made an offer for our last one, use previous.
            thereOffer = _proposalHistory.get(_proposalHistory.size() - 2)[1];
        }
        if (thereOffer == null) return ourOffer;
        // They have made some kind of offer.
        double thereOfferUtil = _utilityFun.evaluate(thereOffer);
        double ourOfferUtil = _utilityFun.evaluate(ourOffer);
        //TODO, Remove
        System.out.println(_ourName + " had initial offer of util: " + ourOfferUtil + " and was offered " + thereOfferUtil + " worth of utility");
        if (thereOfferUtil > ourOfferUtil * 0.9) {
            // We are within %90 utility of what we want. Accept the offer.
            return new PowerSaleAgreement(thereOffer, _timeSlice);
        }
        // Move linearly towards there offer, provided it is within %90 of our best offer.
        return linearMove(ourOffer, thereOffer, 0.9, 1);
    }
    // Recursive function that looks for an offer that is closer to the one offered that is still acceptable.
    // Note : Step size and tolerance are percentages, DO NOT EXCEEDED 0<->1
    @Nullable
    private PowerSaleProposal linearMove(PowerSaleProposal p1, PowerSaleProposal p2,
                                         double tolerance, double stepSize) {
        PowerSaleProposal counter = new PowerSaleProposal(
                (p1.getAmount() - p2.getAmount()) * stepSize,
                (int) Math.round((p1.getDuration() - p2.getDuration()) * stepSize),
                p2.getSellerAID(),
                (p2.getSellerAID().getName().equals(_ourName)));
        counter.setCost((p1.getCost() - p2.getCost()) * stepSize);
        if (_utilityFun.evaluate(p1) * tolerance < _utilityFun.evaluate(counter)) {
            // This counter is pretty good, return it.
            return counter;
        }
        // We have just evaluated the quality of a deal near what the other person wanted, break
        if (withinStepSize(counter, p2, tolerance)) return null;
        // Recursively call till we get to there offer or we find an acceptable one.
        return linearMove(counter, p2, tolerance, stepSize);
    }

    private boolean withinStepSize (PowerSaleProposal p1, PowerSaleProposal p2, double stepSize) {
        return (Math.abs((p1.getAmount() - p2.getAmount())) < stepSize &&
                Math.abs((p1.getCost() - p2.getCost())) < stepSize &&
                Math.abs((p1.getDuration() - p2.getDuration())) < stepSize);
    }
}
