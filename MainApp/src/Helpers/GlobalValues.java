package Helpers;

import java.io.Serializable;

public class GlobalValues implements Serializable{
    private int time;
    private Weather weather;

    public GlobalValues(int t, Weather w) {
        time = t;
        weather = w;
    }

    public Weather getWeather() {
        return weather;
    }

    public int getTime () {
        return time;
    }
}
