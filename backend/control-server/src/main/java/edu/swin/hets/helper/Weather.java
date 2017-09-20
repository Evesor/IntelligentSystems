package edu.swin.hets.helper;

import java.io.Serializable;

/******************************************************************************
 *  Enums for types of weather we can have in our system
 *****************************************************************************/
public enum Weather implements Serializable {
    VerySunny ,Sunny, Overcast, Night;

    public static Weather getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
}
