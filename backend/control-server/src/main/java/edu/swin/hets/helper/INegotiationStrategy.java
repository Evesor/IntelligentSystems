package edu.swin.hets.helper;

/******************************************************************************
 *  Use: To allow agents to handle diffrent negotiation stratergies in a
 *       uniform manner.
 *****************************************************************************/
public interface INegotiationStrategy {
    void changeUtilityFunction(IUtilityFunction utilFun);
    void addNewProposal(PowerSaleProposal proposal, boolean fromUs);
    IPowerSaleContract getResponse();
    String getOpponentName();
}
