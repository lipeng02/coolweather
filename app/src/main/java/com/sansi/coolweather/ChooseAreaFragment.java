package com.sansi.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sansi.coolweather.db.City;
import com.sansi.coolweather.db.County;
import com.sansi.coolweather.db.Province;
import com.sansi.coolweather.util.HttpUtil;
import com.sansi.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private static final String URL = "http://guolin.tech/api/china/";
    private ProgressDialog progressDialog;
    private TextView titleActionBar;
    private Button buttonBack;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> dataList = new ArrayList<>();

    //省级表
    private List<Province> provinceList;

    //市级表
    private List<City> cityList;

    //县级表
    private List<County> countyList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //选中的级别
    private int currentLevel;
    private int provinceCode;
    private int cityCode;
    //拼接后的网址
    private String url;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleActionBar = view.findViewById(R.id.title_actionBar);
        buttonBack = view.findViewById(R.id.button_back);
        listView = view.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(i);
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(i);
                queryCounties();
            }else if (currentLevel==LEVEL_COUNTY){
                String weatherId = countyList.get(i).getWeatherId();
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                intent.putExtra("weather_id",weatherId);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
            }
        });
        buttonBack.setOnClickListener(view -> {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });
        queryProvinces();
    }

    //查询全国所有的省,优先从数据库查询,如果没有查到再去服务器上查
    private void queryProvinces() {
        titleActionBar.setText("中国");
        buttonBack.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
            Toast.makeText(getContext(), "查询自LitePal数据库_province", Toast.LENGTH_SHORT).show();
        } else {
            queryFromServer(URL, "province");
            Toast.makeText(getContext(), "查询自网站服务器_province", Toast.LENGTH_SHORT).show();
        }
    }

    //查询选中省所有的市,优先从数据库查询,如果没有查到再去服务器上查
    private void queryCities() {
        titleActionBar.setText(selectedProvince.getProvinceName());
        buttonBack.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
            Toast.makeText(getContext(), "查询自LitePal数据库_city", Toast.LENGTH_SHORT).show();
        } else {
            provinceCode = selectedProvince.getProvinceCode();
            url = URL + provinceCode;
            queryFromServer(url, "city");
            Toast.makeText(getContext(), "查询自网站服务器_city", Toast.LENGTH_SHORT).show();
        }
    }

    //查询选中市所有的县,优先从数据库查询,如果没有查到再去服务器上查
    private void queryCounties() {
        titleActionBar.setText(selectedCity.getCityName());
        buttonBack.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
            Toast.makeText(getContext(), "查询自LitePal数据库_county", Toast.LENGTH_SHORT).show();
        } else {
            provinceCode = selectedProvince.getProvinceCode();
            cityCode = selectedCity.getCityCode();
            url = URL + provinceCode + "/" + cityCode;
            queryFromServer(url, "county");
            Toast.makeText(getContext(), "查询自网站服务器_county", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryFromServer(String url, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程逻辑
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        switch (type) {
                            case "province":
                                queryProvinces();
                                break;
                            case "city":
                                queryCities();
                                break;
                            case "county":
                                queryCounties();
                                break;
                        }
                    });
                }
            }
        });
    }

    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
