package com.sansi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("wind_spd")
    public String windSpeed;
    @SerializedName("wind_sc")
    public String windScale;
    @SerializedName("wind_dir")
    public String windDirection;

    @SerializedName("cond")
    public More more;

    public static class More {
        @SerializedName("txt")
        public String info;
    }
}
