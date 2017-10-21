package edu.swin.hets.helper.negotiator;

import edu.swin.hets.helper.INegotiationStrategy;
import edu.swin.hets.helper.PowerSaleProposal;
import edu.swin.hets.helper.PowerSaleAgreement;
import edu.swin.hets.helper.IPowerSaleContract;
/******************************************************************************
 *  Use: A common type of negotiator that simply mirrors what the opponent
 *       counters with.
 *****************************************************************************/
public class TitForTatNegotiator implements INegotiationStrategy {
    @Override
    public void addNewProposal(PowerSaleProposal proposal, boolean fromUs) {

    }

    @Override
    public IPowerSaleContract getResponse() {
        return null;
    }

    @Override
    public String getOpponentName() {
        return null;
    }

}
