package com.sansi.coolweather.util;

import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sansi.coolweather.WeatherActivity;
import com.sansi.coolweather.db.City;
import com.sansi.coolweather.db.County;
import com.sansi.coolweather.db.Province;
import com.sansi.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePalApplication;

public class Utility {
    //解析和处理服务器返回的省级数据
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject provinceJSONObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceJSONObject.getString("name"));
                    province.setProvinceCode(provinceJSONObject.getInt("id"));
                    //调用save()方法把数据存储到数据库中
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析和处理服务器返回的市级数据
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray cities = new JSONArray(response);
                for (int i = 0; i < cities.length(); i++) {
                    JSONObject cityJSONObject = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityJSONObject.getString("name"));
                    city.setCityCode(cityJSONObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析和处理服务器返回的县级数据
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray counties = new JSONArray(response);
                for (int i = 0; i < counties.length(); i++) {
                    JSONObject countyJSONObject = counties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyJSONObject.getString("name"));
                    county.setWeatherId(countyJSONObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //将返回的JSON数据解析成Weather实体类
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
