package com.sansi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //用注解方式让JSON字段和Java字段之间建立映射关系
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;

    public static class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
