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
        PowerSaleProposal prop = new PowerSaleProposal(getAmount(), getTime(), getCost(),
                getOurMostRecentOffer().getSellerAID(), getOurMostRecentOffer().getBuyerAID());
        if (_utilityFunction.evaluate(prop) > _utilityFunction.evaluate(getOurMostRecentOffer()) * _accepTolerance)
            return Optional.of(prop);
        return Optional.empty();
    }
    //TODO, Finish
    private double getAmount() {
        if(_proposalHistory.stream().filter((prop) -> prop[1] != null).count() < 1)
            return getOurMostRecentOffer().getAmount();
        return 5;
        // PowerSaleProposal thereSecondMostRecent = _proposalHistory.get(_proposalHistory.size() - 1);
    }

    private int getTime() {
        return 5;
    }

    private double getCost () {
        return 5;
    }
}
