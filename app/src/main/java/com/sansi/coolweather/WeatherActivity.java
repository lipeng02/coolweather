package com.sansi.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.sansi.coolweather.gson.Forecast;
import com.sansi.coolweather.gson.Weather;
import com.sansi.coolweather.util.HttpUtil;
import com.sansi.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView layoutWeather;
    private LinearLayout layoutForecast;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView textDegree;
    private TextView textWeatherInfo;
    private TextView textAqi;
    private TextView textPm25;
    private TextView textQlty;
    private TextView textComfort;
    private TextView textCarWash;
    private TextView textSport;
    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置系统状态栏为透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_weather);
        //初始化各个控件
        layoutWeather = findViewById(R.id.layout_weather);
        layoutForecast = findViewById(R.id.layout_forecast);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_updateTime);
        textDegree = findViewById(R.id.text_degree);
        textWeatherInfo = findViewById(R.id.text_weather_info);
        textAqi = findViewById(R.id.text_aqi);
        textPm25 = findViewById(R.id.text_pm25);
        textQlty = findViewById(R.id.text_qlty);
        textComfort = findViewById(R.id.text_comfort);
        textCarWash = findViewById(R.id.text_car_wash);
        textSport = findViewById(R.id.text_sport);
        bingPicImg = findViewById(R.id.bing_pic_img);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String weatherString = sharedPreferences.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            layoutWeather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bing_pic = sharedPreferences.getString("bing_pic", null);
        if (bing_pic != null) {
            Glide.with(this).load(bing_pic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    //根据天气ID请求城市天气信息
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=c7be98b099dd4c6593e43855884ebd4d";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "未连接网络", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loadBingPic();
    }

    //加载必应每日一图
    private void loadBingPic() {
        String requestBingPicUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPicUrl = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPicUrl);
                editor.apply();
                runOnUiThread(() ->
                        Glide.with(WeatherActivity.this).load(bingPicUrl).into(bingPicImg)
                );
            }
        });
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        textDegree.setText(degree);
        textWeatherInfo.setText(weatherInfo);
        layoutForecast.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, layoutForecast, false);
            TextView textDate = view.findViewById(R.id.text_date);
            TextView textInfo = view.findViewById(R.id.text_info);
            TextView textMax = view.findViewById(R.id.text_max);
            TextView textMin = view.findViewById(R.id.text_min);
            textDate.setText(forecast.date);
            textInfo.setText(forecast.more.info);
            textMax.setText(forecast.temperature.max);
            textMin.setText(forecast.temperature.min);
            layoutForecast.addView(view);
        }
        if (weather.aqi != null) {
            textAqi.setText(weather.aqi.city.aqi);
            textPm25.setText(weather.aqi.city.pm25);
            textQlty.setText(weather.aqi.city.qlty);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        textComfort.setText(comfort);
        textCarWash.setText(carWash);
        textSport.setText(sport);
        layoutWeather.setVisibility(View.VISIBLE);
    }
}
