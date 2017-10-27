package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.*;
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
 *      Good ranges for params:
 *          + maxStallTime 5 - 10
 *          + maxNegotiationTime 15 - 20
 *          + PriceJump 0.1 - 0.5
 *          + VolumeJump 0.1 - 1
 *          + TimeJump 1-2
 *          + Acceptance to 0.9 -> 0.99
 *****************************************************************************/
public class LinearUtilityDecentNegotiator extends NegotiatorBase {
    private IUtilityFunction _utilityFun;
    private double _priceJump;
    private double _volumeJump;
    private double _acceptTolerance;
    private Integer _timeJump;

    public LinearUtilityDecentNegotiator(PowerSaleProposal firstOffer,
                                         String conversationID,
                                         String opponentsName,
                                         GlobalValues currentGlobals,
                                         int maxNegotiationTime,
                                         int maxStallTime,
                                         double priceJump,
                                         double volumeJump,
                                         int timeJump,
                                         double acceptTolerance,
                                         IUtilityFunction utilityFunction) {
        super(opponentsName, conversationID, firstOffer, maxNegotiationTime, maxStallTime, currentGlobals);
        _utilityFun = utilityFunction;
        _priceJump = priceJump;
        _volumeJump = volumeJump;
        _timeJump = timeJump;
        _acceptTolerance = acceptTolerance;
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        PowerSaleProposal ourOffer = getOurMostRecentOffer();
        PowerSaleProposal thereOffer = getThereMostRecentOffer();
        if (conversationStalled()) return Optional.empty();
        if (conversationToLong()) return Optional.empty();
        if (thereOffer == null) return Optional.of(ourOffer);
        // They have made some kind of offer.
        double thereOfferUtil = _utilityFun.evaluate(thereOffer);
        double ourOfferUtil = _utilityFun.evaluate(ourOffer);
        //TODO, Remove
        System.out.println(" had initial offer of util: " + ourOfferUtil +
                " and was offered " + thereOfferUtil + " worth of utility");
        if (thereOfferUtil > ourOfferUtil * _acceptTolerance) {
            // We are within tolerance utility of what we want. Accept the offer.
            return Optional.of(new PowerSaleAgreement(thereOffer, _currentGlobals.getTime()));
        }
        // Move linearly towards there offer, provided it is within tolerance of our last offer.
        return linearMove(ourOffer, thereOffer, _utilityFun, _volumeJump, _timeJump, _priceJump, _acceptTolerance);
    }
}
