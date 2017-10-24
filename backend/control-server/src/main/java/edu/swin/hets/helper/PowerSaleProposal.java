package edu.swin.hets.helper;

import jade.core.AID;
import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a negotiation between two agents.
 *  NOTES: The cost of the contract is set to null on initialization.
 *****************************************************************************/
public class PowerSaleProposal implements Serializable, IPowerSaleContract{
    private double _power_amount;
    private int _duration;
    private double _cost;
    private AID _seller_AID;
    private AID _buyer_AID;

    public PowerSaleProposal(double powerAmount, int lengthOfContract,double cost , AID your_id, boolean seller) {
        if (seller) _seller_AID = your_id;
        else _buyer_AID = your_id;
        _power_amount = powerAmount;
        _duration = lengthOfContract;
        _cost = cost;
    }

    public boolean withinTolorance (PowerSaleProposal p2, double powerTol,double durationTol, double costTol) {
        if (Math.abs(_power_amount - _power_amount * powerTol) > p2.getAmount() ||
                Math.abs(_power_amount + _power_amount * powerTol) < p2.getAmount()) return false;
        if (Math.abs(_duration - _duration * durationTol) > p2.getDuration() ||
                Math.abs(_duration + _duration * durationTol) < p2.getDuration()) return false;
        if (Math.abs(_cost - _cost * costTol) > p2.getCost() ||
                Math.abs(_cost + _cost * costTol) < p2.getCost()) return false;
        return true;
    }
    //Used to check if two contracts are equal, will give true if contracts can be made to be same.
    public boolean equalValues(PowerSaleProposal prop) {
        if (prop._buyer_AID != null && _buyer_AID != null) {
            if (!prop._buyer_AID.getName().equals(_buyer_AID.getName())) return false;
        }
        if (prop._seller_AID != null && _seller_AID != null) {
            if (!prop._seller_AID.getName().equals(_seller_AID.getName())) return false;
        }
        if (prop._cost != _cost) return false;
        if (prop._duration != _duration) return false;
        if (prop._power_amount != _power_amount) return false;
        return true;
    }
    // Getters
    public double getAmount() { return _power_amount; }
    public int getDuration() { return _duration; }
    public double getCost() { return _cost; }
    public AID getSellerAID() { return _seller_AID; }
    public AID getBuyerAID() { return _buyer_AID; }
    // Setters
    public void setSellerAID (AID aid) { _seller_AID = aid; }
    public void setBuyerAID (AID aid) { _buyer_AID = aid; }
    // Used to get a details of object in JSON form
    public String getJSON() {
        return "Not implemented";
    }
}
