package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.*;
import java.util.Optional;

/******************************************************************************
 *  Use: A common type of negotiator that simply mirrors what the opponent
 *       counters with provided the Utility of the counter is equil to or better
 *       than the utility of our first offer.
 *****************************************************************************/
public class TitForTatNegotiator extends NegotiatorBase {
    private IUtilityFunction _utilityFunction;
    private double _accepTolerance;

    public TitForTatNegotiator (PowerSaleProposal firstOffer,
                                String conversationID,
                                String opponentsName,
                                GlobalValues currentGlobals,
                                int maxNegotiationTime,
                                int maxStallTime,
                                double acceptTolerance,
                                IUtilityFunction utilityFunction) {
        super(opponentsName, conversationID, firstOffer, maxNegotiationTime, maxStallTime, currentGlobals);
        _utilityFunction = utilityFunction;
        _accepTolerance = acceptTolerance;
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        if(conversationStalled()) return Optional.empty();
        if(conversationToLong()) return Optional.empty();
        if (getThereMostRecentOffer() == null) return Optional.of(getOurMostRecentOffer());
        // Change our offer by what they last changed by and offer if its within tolerance
        if(_proposalHistory.stream().filter((prop) -> prop[1] != null).count() < 1)
            return Optional.of(getOurMostRecentOffer());
        PowerSaleProposal newOffer = getOffer();
        if (_utilityFunction.evaluate(newOffer) > _utilityFunction.evaluate(getOurMostRecentOffer()) * _accepTolerance)
            return Optional.of(newOffer);
        return Optional.empty();
    }

    private PowerSaleProposal getOffer() {
        PowerSaleProposal thereSecondMostRecent = _proposalHistory.get(_proposalHistory.size() - 1)[1];
        PowerSaleProposal thereMostRecentOffer = getThereMostRecentOffer();
        PowerSaleProposal ourMostRecentOffer = getOurMostRecentOffer();
        double powerAmount = ourMostRecentOffer.getAmount() +
                (thereSecondMostRecent.getAmount() - thereMostRecentOffer.getAmount());
        int length = ourMostRecentOffer.getDuration() +
                (thereSecondMostRecent.getDuration() - thereMostRecentOffer.getDuration());
        double cost = ourMostRecentOffer.getCost() +
                (thereSecondMostRecent.getCost() - thereMostRecentOffer.getCost());
        // Change by what they last changed by.
        return new PowerSaleProposal(powerAmount,length ,cost, ourMostRecentOffer.getSellerAID(),
                ourMostRecentOffer.getBuyerAID());
    }
}
