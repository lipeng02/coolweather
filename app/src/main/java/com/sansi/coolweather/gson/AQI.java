package com.sansi.coolweather.gson;

public class AQI {
    public AQICity city;

    public static class AQICity {
        public String aqi;
        public String pm25;
        public String qlty;
    }
}
