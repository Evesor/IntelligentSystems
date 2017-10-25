package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.*;

import java.util.Optional;

/******************************************************************************
 *  Use:
 *  Notes: See negotiator factory for how to parametrize inputs
 *      Good ranges for params:
 *          + maxStallTime 5 - 10
 *          + maxNegotiationTime 15 - 20
 *          + PriceJump 0.1 - 0.5
 *          + VolumeJump 0.1 - 1
 *          + TimeJump 1-2
 *          + Acceptance to 0.9 -> 0.99
 *****************************************************************************/
public class BoulwareNegotiator extends NegotiatorBase {
    private PowerSaleProposal _firstOffer;
    private double _volumeJump;
    private double _priceJump;
    private int _timeJump;
    private double _acceptanceTolerance;
    private IUtilityFunction _utilityFunction;

    public BoulwareNegotiator(PowerSaleProposal firstOffer,
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
        super(opponentsName, conversationID, firstOffer, maxNegotiationTime ,maxStallTime, currentGlobals);
        _firstOffer = firstOffer;
        _priceJump = priceJump;
        _volumeJump = volumeJump;
        _timeJump = timeJump;
        _utilityFunction = utilityFunction;
        _acceptanceTolerance = acceptTolerance;
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        if (conversationStalled()) return Optional.empty(); //In loop
        if (conversationToLong()) return Optional.empty(); //Used to much time.
        if (_currentGlobals.getTimeLeft() < (GlobalValues.lengthOfTimeSlice() / 3)) {
            // We have a third of a slice left, lets make a better offer.
            return betterOffer();
        }
        return Optional.of(_firstOffer);
    }

    private Optional<IPowerSaleContract> betterOffer() {
        return linearMove(_firstOffer, getThereMostRecentOffer(), _utilityFunction, _volumeJump,
                _timeJump, _priceJump, _acceptanceTolerance);
    }
}
