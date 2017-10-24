package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.INegotiationStrategy;
import edu.swin.hets.helper.PowerSaleProposal;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.IPowerSaleContract;
import edu.swin.hets.helper.IUtilityFunction;
import jade.core.AID;

import java.util.ArrayList;
import java.util.Optional;

/******************************************************************************
 *  Use: A basic implementation of negotiating to test interface.
 *  Notes:
 *       - Repeated calls to LinearUtilityDecentNegotiator without adding new
 *         offers will lead to the same response coming back each time.
 *       - The jump parameters refer to how big a change we should have
 *         between each offer. Smaller values may lead to more optimum
 *         solutions but with longer negotiation times.
 *****************************************************************************/
public class LinearUtilityDecentNegotiator extends NegotiatorBase {
    private IUtilityFunction _utilityFun;
    private String _ourName;
    private Integer _timeSlice;
    private double _priceJump;
    private double _volumeJump;
    private double _acceptTolerance;
    private Integer _timeJump;
    //TODO, put hard limit on negotiation lengths.

    public LinearUtilityDecentNegotiator(IUtilityFunction utilityFun,
                                         String ourName,
                                         String opponentName,
                                         PowerSaleProposal firstOffer,
                                         Integer timeSlice,
                                         double priceJump,
                                         double volumeJump,
                                         Integer timeJump,
                                         double acceptTolerance,
                                         String conversationID) {
        super(opponentName, conversationID, firstOffer);
        _utilityFun = utilityFun;
        _ourName = ourName;
        _timeSlice = timeSlice;
        _priceJump = priceJump;
        _volumeJump = volumeJump;
        _timeJump = timeJump;
        _acceptTolerance = acceptTolerance;
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        PowerSaleProposal ourOffer = getOurMostRecentOffer();
        PowerSaleProposal thereOffer = getThereMostRecentOffer();
        if (thereOffer == null) return Optional.of(ourOffer);
        if (ourOffer == null) System.out.println("*************errror *******************");
        // They have made some kind of offer.
        double thereOfferUtil = _utilityFun.evaluate(thereOffer);
        double ourOfferUtil = _utilityFun.evaluate(ourOffer);
        //TODO, Remove
        System.out.println(_ourName + " had initial offer of util: " + ourOfferUtil +
                " and was offered " + thereOfferUtil + " worth of utility");
        if (thereOfferUtil > ourOfferUtil * _acceptTolerance) {
            // We are within tolerance utility of what we want. Accept the offer.
            return Optional.of(new PowerSaleAgreement(thereOffer, _timeSlice));
        }
        // Move linearly towards there offer, provided it is within %90 of our best offer.
        return linearMove(ourOffer, thereOffer, _acceptTolerance);
    }

    // Recursive function that looks for an offer that is closer to the one offered that is still acceptable.
    // Note : Step size and tolerance are percentages, DO NOT EXCEEDED 0<->1
    private Optional<IPowerSaleContract> linearMove(PowerSaleProposal ourProposal,
                                                    PowerSaleProposal thereProposal,
                                                    double tolerance) {
        PowerSaleProposal counter = new PowerSaleProposal(
                changeVolume(thereProposal, ourProposal, _volumeJump),
                changeTime(thereProposal, ourProposal, _timeJump),
                changeCost(thereProposal, ourProposal, _priceJump),
                ourProposal.getSellerAID(),
                ourProposal.getBuyerAID());
        if (_utilityFun.evaluate(ourProposal) * tolerance < _utilityFun.evaluate(counter)) {
            // This counter is pretty good, return it.
            return Optional.of(counter);
        }
        // We have just evaluated the quality of a deal near what the other person wanted, break
        if (withinStepSize(counter, thereProposal)) return Optional.empty();
        // Recursively call till we get to there offer or we find an acceptable one.
        return linearMove(counter, thereProposal, tolerance);
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

    private boolean withinStepSize (PowerSaleProposal p1, PowerSaleProposal p2) {
        return (Math.abs((p1.getAmount() - p2.getAmount())) < _volumeJump &&
                Math.abs((p1.getCost() - p2.getCost())) < _priceJump &&
                Math.abs((p1.getDuration() - p2.getDuration())) < _timeJump);
    }
}
