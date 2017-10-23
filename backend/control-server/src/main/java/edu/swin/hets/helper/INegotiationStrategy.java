package edu.swin.hets.helper;

import java.util.Optional;
/******************************************************************************
 *  Use: To allow agents to handle diffrent negotiation stratergies in a
 *       uniform manner.
 *****************************************************************************/
public interface INegotiationStrategy {
    void addNewProposal(PowerSaleProposal proposal, boolean fromUs);
    Optional<IPowerSaleContract> getResponse();
    String getOpponentName();
}
