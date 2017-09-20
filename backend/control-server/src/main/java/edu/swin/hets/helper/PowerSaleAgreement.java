package edu.swin.hets.helper;

import jade.core.AID;

import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a contract agreed to by two agents.
 *****************************************************************************/
public class PowerSaleAgreement implements Serializable{
    private double _power_amount;
    private int _start_time;
    private int _end_time;
    private double _cost;
    private AID _seller_AID;
    private AID _buyer_AID;

    public PowerSaleAgreement(double amount, int start, int finish, double cost, AID buyerAID, AID sellerAID) {
        _power_amount = amount;
        _start_time = start;
        _end_time = finish;
        _cost = cost;
        _buyer_AID = buyerAID;
        _seller_AID = sellerAID;
    }

    public PowerSaleAgreement(PowerSaleProposal prop, int time_now) {
        this(prop.getAmount(), time_now, time_now + prop.getDuration(),
                prop.getCost(), prop.getBuyerAID(), prop.getSellerAID());
    }

    public double getAmount() { return _power_amount; }
    public int getEndTime() { return _end_time; }
    public int getStartTime() { return _start_time; }
    public double getCost() { return _cost; }
    public AID getSellerAID() { return _seller_AID; }
    public AID getBuyerAID() { return _buyer_AID; }
    // Setters
    public void setSellerAID (AID aid) { _seller_AID = aid; }
    public void setBuyerAID (AID aid) { _buyer_AID = aid; }
    // Used by the webserver agent to get the JSON string describing this event.
    public String getJSON () {
        return "Not implemented";
    }
}
