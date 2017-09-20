package edu.swin.hets.helper;

import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a contract agreed to by two agents.
 *****************************************************************************/
public class PowerSaleAgreement implements Serializable{
    private double _power_amount;
    private int _start_time;
    private int _end_time;

    public  PowerSaleAgreement(PowerSaleProposal prop, int time_now) {
        _power_amount = prop.getAmount();
        _start_time = time_now;
        _end_time = time_now + prop.getDuration();
    }

    public PowerSaleAgreement(float amount, int start, int finish) {
        _power_amount = amount;
        _start_time = start;
        _end_time = finish;
    }

    public double getAmount() { return _power_amount; }
    public int getEndTime() { return _end_time; }
    public int getStartTime() { return _start_time; }
}
