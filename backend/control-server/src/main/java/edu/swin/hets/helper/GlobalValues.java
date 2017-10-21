package edu.swin.hets.helper;

import java.io.Serializable;


public class GlobalValues implements Serializable{
    private int _time;
    private int _time_left;
    private Weather _weather;

    public GlobalValues(int t, Weather w, int timeLeft) {
        _time = t;
        _weather = w;
        _time_left = timeLeft;
    }

    public Weather getWeather() {
        return _weather;
    }
    public int getTime () {
        return _time;
    }
    public int getTimeLeft () { return _time_left; }
    public static int lengthOfTimeSlice () { return 5000;}
    public static int pushTimeLength () { return 1000; }
}
