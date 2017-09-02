package Helpers;

import java.io.Serializable;
/******************************************************************************
 *  Use: To hold the details of a contract agreed to by two agents.
 *****************************************************************************/
public class PowerSaleAgreement implements Serializable{
    float _power_amount;
    Time _start_time;
    Time _end_time;

    public PowerSaleAgreement(float amount, Time start, Time finish) {
        _power_amount = amount;
        _start_time = start;
        _end_time = finish;
    }
}
