package edu.swin.hets.helper;

/******************************************************************************
 *  Use: To allow negotiation strategies to handel different user desires
 *       in a uniform manner.
 *****************************************************************************/
public interface IUtilityFunction {
    double evaluate(PowerSaleProposal proposal);
    boolean equals(IUtilityFunction utility);
}
