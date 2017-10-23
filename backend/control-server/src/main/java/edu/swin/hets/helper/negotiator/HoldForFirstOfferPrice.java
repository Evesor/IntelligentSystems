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
            // Messy because seller and buyer may not both be initialized.
            if (_firstOffer.getSellerAID() != null && _firstOffer.getBuyerAID() != null) {
                // Both filled out, switch on type
                if (_firstOffer.getSellerAID().getName().equals(_opponentsName)) return weAreBuyer();
                else if (_firstOffer.getBuyerAID().getName().equals(_opponentsName)) return weAreSeller();
            }
            else {
                if (_firstOffer.getBuyerAID() == null) return weAreSeller();
                else if (_firstOffer.getSellerAID() == null) return weAreBuyer();
            }
            //TODO, add a throw or something, if we get here that is bad.
        }
        return _firstOffer;
    }

    private IPowerSaleContract weAreBuyer() {
        if (_thereMostRecentOffer.getCost() <= _firstOffer.getCost())
            return new PowerSaleAgreement(_thereMostRecentOffer, _currentTime);
        return _firstOffer;
    }

    private IPowerSaleContract weAreSeller() {
        if (_thereMostRecentOffer.getCost() >= _firstOffer.getCost())
            return new PowerSaleAgreement(_thereMostRecentOffer, _currentTime);
        return _firstOffer;
    }

    @Override
    public String getOpponentName() {
        return _opponentsName;
    }
}
