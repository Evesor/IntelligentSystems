package edu.swin.hets.helper;

import jade.core.AID;

import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a negotiation between two agents.
 *  NOTES: The cost of the contract is set to a negative number on
 *         initialization.
 *****************************************************************************/
public class PowerSaleProposal implements Serializable {
    private double _power_amount;
    private int _duration;
    private double _cost;
    private AID _seller_AID;
    private AID _buyer_AID;

    public PowerSaleProposal(double powerAmount, int lengthOfContract) {
        _power_amount = powerAmount;
        _duration = lengthOfContract;
        _cost = -1;
    }
    // Getters
    public double getAmount() { return _power_amount; }
    public int getDuration() { return _duration; }
    public double getCost() {
        return _cost;
    }
    public AID getSellerAID() { return _seller_AID; }
    public AID getBuyerAID() { return _buyer_AID; }
    // Setters
    public void setCost(double cost) {
        _cost = cost;
    }
    public void setSellerAID (AID aid) { _seller_AID = aid; }
    public void setBuyerAID (AID aid) { _buyer_AID = aid; }
    // Used to get a details of object in JSON form
    public String getJSON() {
        return "Not implemented";
    }
}
