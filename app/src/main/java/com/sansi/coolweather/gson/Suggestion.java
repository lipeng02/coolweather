package com.sansi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    public Sport sport;

    @SerializedName("cw")
    public CarWash carWash;

    public static class Comfort {
        @SerializedName("txt")
        public String info;
    }

    public static class Sport {
        @SerializedName("txt")
        public String info;
    }

    public static class CarWash {
        @SerializedName("txt")
        public String info;
    }
}
