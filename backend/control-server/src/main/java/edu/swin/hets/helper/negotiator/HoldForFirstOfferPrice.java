package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.INegotiationStrategy;
import edu.swin.hets.helper.IPowerSaleContract;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;
/******************************************************************************
 *  Use: A very basic strategy where we don't care about how the contract gets
 *       changed so long as the price remains the same as our first offer.
 *       Otherwise will return the same offer.
 *****************************************************************************/
public class HoldForFirstOfferPrice implements INegotiationStrategy {
    private PowerSaleProposal _firstOffer;
    private PowerSaleProposal _thereMostRecentOffer;
    private String _opponentsName;
    private Integer _currentTime;

    public HoldForFirstOfferPrice(PowerSaleProposal firstOffer, String opponentsName, Integer currentTime) {
        _firstOffer = firstOffer;
        _opponentsName = opponentsName;
        _thereMostRecentOffer = null;
        _currentTime = currentTime;
    }

    @Override
    public void addNewProposal(PowerSaleProposal proposal, boolean fromUs) {
        if (!fromUs) _thereMostRecentOffer = proposal;
    }

    @Override
    public IPowerSaleContract getResponse() {
        if (_thereMostRecentOffer != null) {
            if (_thereMostRecentOffer.getCost() >= _firstOffer.getCost()) {
                return new PowerSaleAgreement(_thereMostRecentOffer, _currentTime);
            }
        }
        return _firstOffer;
    }

    @Override
    public String getOpponentName() {
        return _opponentsName;
    }

}
