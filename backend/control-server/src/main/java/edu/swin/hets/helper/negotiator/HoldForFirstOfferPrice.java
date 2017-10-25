package edu.swin.hets.helper.negotiator;

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
    private Integer _currentTime;
    private Integer _maxNegotiationTime;
    private double _volumeTolerance;
    private double _costTolerance;
    private double _durationTolerance;

    public HoldForFirstOfferPrice(PowerSaleProposal firstOffer,
                                  String conversationID,
                                  String opponentsName,
                                  Integer currentTime,
                                  Integer maxNegotiationTime,
                                  double costTolerance,
                                  double volumeTolerance,
                                  double durationTolerance) {
        super(opponentsName, conversationID, firstOffer);
        _firstOffer = firstOffer;
        _currentTime = currentTime;
        _maxNegotiationTime = maxNegotiationTime;
        _costTolerance = costTolerance;
        _durationTolerance = durationTolerance;
        _volumeTolerance = volumeTolerance;
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        if (getThereMostRecentOffer() == null) return Optional.of(getOurMostRecentOffer());
        if (sameAsLastNOffers(getThereMostRecentOffer(), 5)) return Optional.empty(); //Stop negotiation.
        if (getNumberOfProps() > _maxNegotiationTime) return Optional.empty();
        if (_firstOffer.getSellerAID().getName().equals(getOpponentName())) return responseWhenWeAreBuyer();
        else if (_firstOffer.getBuyerAID().getName().equals(getOpponentName())) return responseWhenWeAreSeller();
        return Optional.of(_firstOffer);
    }

    private Optional<IPowerSaleContract> responseWhenWeAreBuyer() {
        if (getThereMostRecentOffer() != null) {
            if (getThereMostRecentOffer().getCost() <= _firstOffer.getCost() &&
                    _firstOffer.withinTolorance(getThereMostRecentOffer(), _volumeTolerance, _durationTolerance,
                            _costTolerance)) return Optional.of(new PowerSaleAgreement(getThereMostRecentOffer(),
                    _currentTime));
            return Optional.of(_firstOffer);
        }
        return Optional.empty();
    }

    private Optional<IPowerSaleContract> responseWhenWeAreSeller() {
        PowerSaleProposal thereMostRecentOffer = getThereMostRecentOffer();
        if (thereMostRecentOffer != null) {
            if (thereMostRecentOffer.getCost() >= _firstOffer.getCost() &&
                    _firstOffer.withinTolorance(getThereMostRecentOffer(), 0.5, 0.5, 1))
                return Optional.of(new PowerSaleAgreement(getThereMostRecentOffer(), _currentTime));
            return Optional.of(_firstOffer);
        }
        return Optional.empty();
    }
}
