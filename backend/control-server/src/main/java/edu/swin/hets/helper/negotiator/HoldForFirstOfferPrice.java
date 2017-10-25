package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.GlobalValues;
import edu.swin.hets.helper.IPowerSaleContract;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;
import java.util.Optional;

/******************************************************************************
 *  Use: A very basic strategy where we don't care about how the contract gets
 *       changed so long as the price remains the same as our first offer.
 *       Otherwise will return the same offer.
 * Note: Returns null when we should terminate the conversation.
 *****************************************************************************/
public class HoldForFirstOfferPrice extends NegotiatorBase {
    private PowerSaleProposal _firstOffer;
    private double _volumeTolerance;
    private double _costTolerance;
    private double _timeTolerance;

    public HoldForFirstOfferPrice(PowerSaleProposal firstOffer,
                                  String conversationID,
                                  String opponentsName,
                                  GlobalValues currentGlobals,
                                  int maxNegotiationTime,
                                  int maxStallTime,
                                  double costTolerance,
                                  double volumeTolerance,
                                  double timeTolerance) {
        super(opponentsName, conversationID, firstOffer, maxNegotiationTime, maxStallTime, currentGlobals);
        _firstOffer = firstOffer;
        _costTolerance = costTolerance;
        _timeTolerance = timeTolerance;
        _volumeTolerance = volumeTolerance;
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        if (getThereMostRecentOffer() == null) return Optional.of(getOurMostRecentOffer());
        if (conversationStalled()) return Optional.empty(); //Stop negotiation.
        if (conversationToLong()) return Optional.empty(); //Stop negotiation.
        if (areWeSeller()) return responseWhenWeAreSeller();
        else return responseWhenWeAreBuyer();
    }

    private Optional<IPowerSaleContract> responseWhenWeAreBuyer() {
        if (getThereMostRecentOffer() != null) {
            if (getThereMostRecentOffer().getCost() <= _firstOffer.getCost() &&
                    _firstOffer.withinTolorance(getThereMostRecentOffer(),
                            _volumeTolerance, _timeTolerance, _costTolerance))
                    return Optional.of(new PowerSaleAgreement(getThereMostRecentOffer(), _currentGlobals.getTime()));
            return Optional.of(_firstOffer);
        }
        return Optional.empty();
    }
//TODO, fix
    private Optional<IPowerSaleContract> responseWhenWeAreSeller() {
        PowerSaleProposal thereMostRecentOffer = getThereMostRecentOffer();
        if (thereMostRecentOffer != null) {
            if (getThereMostRecentOffer().getCost() >= _firstOffer.getCost() &&
                    _firstOffer.withinTolorance(getThereMostRecentOffer(),
                            _volumeTolerance, _timeTolerance, _costTolerance))
                return Optional.of(new PowerSaleAgreement(getThereMostRecentOffer(), _currentGlobals.getTime()));
            return Optional.of(_firstOffer);
        }
        return Optional.empty();
    }
}
