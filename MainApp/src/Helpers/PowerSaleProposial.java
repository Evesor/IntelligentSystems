package Helpers;

import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a contract agreed to by two agents.
 *****************************************************************************/
public class PowerSaleProposial implements Serializable {
    private float _power_amount;
    private int _durration;
    private float _cost;

    public PowerSaleProposial(float amount, int length, float cost) {
        _power_amount = amount;
        _durration = length;
        _cost = cost;
    }
    public float getAmount() {
        return _power_amount;
    }
    public float getDuration() {
        return _power_amount;
    }
    public float getCost() {
        return _cost;
    }
    public void setAmount(float amount) {
        _power_amount = _power_amount;
    }
    public void setDuration(int durration) {
        _durration = durration;
    }
    public void setCost(float cost) {
        _cost = cost;
    }
}
