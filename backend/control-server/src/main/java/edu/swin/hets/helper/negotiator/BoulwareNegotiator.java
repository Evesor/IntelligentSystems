package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.IPowerSaleContract;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;

import java.util.Optional;

/******************************************************************************
 *  Use:
 *****************************************************************************/
public class BoulwareNegotiator extends NegotiatorBase {
    private PowerSaleProposal _firstOffer;
    private Integer _currentTime;
    private Integer _maxNegotiationTime;
    private double _volumeTolerance;
    private double _costTolerance;
    private double _durationTolerance;

    public BoulwareNegotiator(PowerSaleProposal firstOffer,
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
        if (getThereMostRecentOffer() == null) return Optional.empty();
        if (sameAsLastNOffers(getThereMostRecentOffer(), 5)) return Optional.empty();
        if (getNumberOfProps() > _maxNegotiationTime) return Optional.empty();
        if (getNumberOfProps() > 1) {
            // Messy because seller and buyer may not both be initialized.
            if (_firstOffer.getSellerAID() != null && _firstOffer.getBuyerAID() != null) {
                // Both filled out, switch on type
                if (_firstOffer.getSellerAID().getName().equals(getOpponentName())) return responseWhenWeAreBuyer();
                else if (_firstOffer.getBuyerAID().getName().equals(getOpponentName())) return responseWhenWeAreSeller();
            }
            else {
                if (_firstOffer.getBuyerAID() == null) return responseWhenWeAreSeller();
                else if (_firstOffer.getSellerAID() == null) return responseWhenWeAreBuyer();
            }
            //TODO, add a throw or something, if we get here that is bad.
        }
        return Optional.of(_firstOffer);
    }

    private Optional<IPowerSaleContract> responseWhenWeAreBuyer() {
        if (getThereMostRecentOffer() != null) {
            if (getThereMostRecentOffer().getCost() == null &&
                    _firstOffer.withinTolorance(getThereMostRecentOffer(), _volumeTolerance, _durationTolerance,
                            _costTolerance)) {
                // What they want to negotiate for is close to what we want except cost.
                PowerSaleProposal counter = getThereMostRecentOffer();
                counter.setCost(_firstOffer.getCost());
                return Optional.of(counter);
            }
            if (getThereMostRecentOffer().getCost() == null) return Optional.empty(); // Stop negotiating
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
            if (thereMostRecentOffer.getCost() == null &&
                    _firstOffer.withinTolorance(thereMostRecentOffer, 0.5, 0.5, 1)) {
                PowerSaleProposal counter = thereMostRecentOffer;
                counter.setCost(_firstOffer.getCost());
                return Optional.of(counter);
            }
            if (thereMostRecentOffer.getCost() == null) return Optional.empty(); //Stop negotiating.
            if (thereMostRecentOffer.getCost() >= _firstOffer.getCost() &&
                    _firstOffer.withinTolorance(getThereMostRecentOffer(), 0.5, 0.5, 1))
                return Optional.of(new PowerSaleAgreement(getThereMostRecentOffer(), _currentTime));
            return Optional.of(_firstOffer);
        }
        return Optional.empty();
    }

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
}
