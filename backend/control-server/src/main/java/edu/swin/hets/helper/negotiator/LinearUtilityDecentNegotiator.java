package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.INegotiationStrategy;
import edu.swin.hets.helper.PowerSaleProposal;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.IPowerSaleContract;
import edu.swin.hets.helper.IUtilityFunction;
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
 *****************************************************************************/
public class LinearUtilityDecentNegotiator implements INegotiationStrategy {
    private ArrayList<PowerSaleProposal[]> _proposalHistory; // Will always be a 2 wide array
    private IUtilityFunction _utilityFun;
    private String _ourName;
    private Integer _timeSlice;
    private String _opponentName;
    private double _priceJump;
    private double _volumeJump;
    private Integer _timeJump;

    public LinearUtilityDecentNegotiator(IUtilityFunction utilityFun,
                                         String ourName,
                                         String opponentName,
                                         PowerSaleProposal firstOffer,
                                         Integer timeSlice,
                                         double priceJump,
                                         double volumeJump,
                                         Integer timeJump) {
        _utilityFun = utilityFun;
        _proposalHistory = new ArrayList<>();
        _proposalHistory.add(new PowerSaleProposal[2]);
        _proposalHistory.get(0)[0] = firstOffer;
        _ourName = ourName;
        _timeSlice = timeSlice;
        _opponentName = opponentName;
        _priceJump = priceJump;
        _volumeJump = volumeJump;
        _timeJump = timeJump;
    }

    public String getOpponentName() { return _opponentName; }

    @Override
    public void addNewProposal(PowerSaleProposal proposal, boolean fromUs) {
        if (fromUs) {
            _proposalHistory.add(new PowerSaleProposal[2]);
            _proposalHistory.get(_proposalHistory.size() - 1)[0] = proposal;
        }
        else {
            _proposalHistory.get(_proposalHistory.size() - 1)[1] = proposal;
        }
    }

    @Override
    public Optional<IPowerSaleContract> getResponse() {
        PowerSaleProposal ourOffer = _proposalHistory.get(_proposalHistory.size() - 1)[0];
        PowerSaleProposal thereOffer = _proposalHistory.get(_proposalHistory.size() - 1)[1];
        if (thereOffer == null && _proposalHistory.size() > 1) {
            // They have not made an offer for our last one, use previous.
            thereOffer = _proposalHistory.get(_proposalHistory.size() - 2)[1];
        }
        if (thereOffer == null) return Optional.of(ourOffer);
        // They have made some kind of offer.
        double thereOfferUtil = _utilityFun.evaluate(thereOffer);
        double ourOfferUtil = _utilityFun.evaluate(ourOffer);
        //TODO, Remove
        System.out.println(_ourName + " had initial offer of util: " + ourOfferUtil +
                " and was offered " + thereOfferUtil + " worth of utility");
        if (thereOfferUtil > ourOfferUtil * 0.9) {
            // We are within %90 utility of what we want. Accept the offer.
            return Optional.of(new PowerSaleAgreement(thereOffer, _timeSlice));
        }
        // Move linearly towards there offer, provided it is within %90 of our best offer.
        return linearMove(ourOffer, thereOffer, 0.9);
    }
    // Recursive function that looks for an offer that is closer to the one offered that is still acceptable.
    // Note : Step size and tolerance are percentages, DO NOT EXCEEDED 0<->1
    private Optional<IPowerSaleContract> linearMove(PowerSaleProposal p1, PowerSaleProposal p2, double tolerance) {
        PowerSaleProposal counter = new PowerSaleProposal(
                (p1.getAmount() - p2.getAmount()) * _volumeJump,
                (int) Math.round((p1.getDuration() - p2.getDuration()) * _timeJump),
                p2.getSellerAID(),
                (p2.getSellerAID().getName().equals(_ourName)));
        counter.setCost((p1.getCost() - p2.getCost()) * _priceJump);
        if (_utilityFun.evaluate(p1) * tolerance < _utilityFun.evaluate(counter)) {
            // This counter is pretty good, return it.
            return Optional.of(counter);
        }
        // We have just evaluated the quality of a deal near what the other person wanted, break
        if (withinStepSize(counter, p2)) return Optional.empty();
        // Recursively call till we get to there offer or we find an acceptable one.
        return linearMove(counter, p2, tolerance);
    }

    private boolean withinStepSize (PowerSaleProposal p1, PowerSaleProposal p2) {
        return (Math.abs((p1.getAmount() - p2.getAmount())) < _volumeJump &&
                Math.abs((p1.getCost() - p2.getCost())) < _priceJump &&
                Math.abs((p1.getDuration() - p2.getDuration())) < _timeJump);
    }
}
