package Helpers;

import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a negotiation between two agents.
 *  NOTES: The cost of the contract is set to a negative number on
 *         initialization.
 *****************************************************************************/
public class PowerSaleProposal implements Serializable {
    private double _power_amount;
    private int _durration;
    private double _cost;

    public PowerSaleProposal(double amount, int length) {
        _power_amount = amount;
        _durration = length;
        _cost = -1;
    }
    public double getAmount() {
        return _power_amount;
    }
    public int getDuration() { return _durration; }
    public double getCost() {
        return _cost;
    }
    public void setAmount(double amount) {
        _power_amount = _power_amount;
    }
    public void setDuration(int durration) {
        _durration = durration;
    }
    public void setCost(double cost) {
        _cost = cost;
    }
}
