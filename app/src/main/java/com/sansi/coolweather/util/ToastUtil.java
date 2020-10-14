package com.sansi.coolweather.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    //单例对象
    private static Toast toast;

    //单例toast显示模式
    public static void showSingleInstance(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    //toast短时间显示
    public static void showShort(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    //toast长时间显示
    public static void showLong(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }
}
