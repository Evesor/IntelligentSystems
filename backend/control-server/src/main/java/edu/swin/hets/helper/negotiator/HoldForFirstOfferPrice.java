package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.INegotiationStrategy;
import edu.swin.hets.helper.IPowerSaleContract;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.PowerSaleProposal;

import java.util.ArrayList;
import java.util.List;
/******************************************************************************
 *  Use: A very basic strategy where we don't care about how the contract gets
 *       changed so long as the price remains the same as our first offer.
 *       Otherwise will return the same offer.
 * Note: Returns null when we should terminate the conversation
 *****************************************************************************/
public class HoldForFirstOfferPrice implements INegotiationStrategy {
    private PowerSaleProposal _firstOffer;
    private List<PowerSaleProposal> _thereOffers;
    private String _opponentsName;
    private Integer _currentTime;

    public HoldForFirstOfferPrice(PowerSaleProposal firstOffer, String opponentsName, Integer currentTime) {
        _firstOffer = firstOffer;
        _opponentsName = opponentsName;
        _currentTime = currentTime;
        _thereOffers = new ArrayList<>();
    }

    @Override
    public void addNewProposal(PowerSaleProposal proposal, boolean fromUs) {
        if (!fromUs) _thereOffers.add(proposal);
    }

    @Override
    public IPowerSaleContract getResponse() {
        if (sameAsLastNOffers(mostRecentOffer(), 5)) return null;
        if (_thereOffers.size() > 1) {
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
        if (mostRecentOffer().getCost() <= _firstOffer.getCost())
            return new PowerSaleAgreement(mostRecentOffer(), _currentTime);
        return _firstOffer;
    }

    private IPowerSaleContract weAreSeller() {
        if (mostRecentOffer().getCost() >= _firstOffer.getCost())
            return new PowerSaleAgreement(mostRecentOffer(), _currentTime);
        return _firstOffer;
    }

    private boolean sameAsLastNOffers (PowerSaleProposal prop, int n) {
        int firstIndex = _thereOffers.size() - 1;
        int lastIndex = firstIndex - n;
        for (int i = firstIndex; i < lastIndex; i++) if (prop.equalValues(_thereOffers.get(i))) return false;
        return true;
    }

    private PowerSaleProposal mostRecentOffer() {
        return _thereOffers.get(_thereOffers.size() - 1);
    }

    @Override
    public String getOpponentName() {
        return _opponentsName;
    }
}
