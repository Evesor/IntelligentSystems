package Helpers;

import java.io.Serializable;
import jade.lang.acl.ACLMessage;
/******************************************************************************
 *  Use: To keep time handling consistent across the system.
 *****************************************************************************/
public class Time implements Serializable {
    private int _time_stamp;

    public Time (int offset_from_now) {
        _time_stamp = getCurrentTime() + offset_from_now;
    }

    public Time() {
        this(0);
    }

    private int getCurrentTime() {

        return 1;
    }
}
