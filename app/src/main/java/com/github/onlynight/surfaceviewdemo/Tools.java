package com.github.onlynight.surfaceviewdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang on 2016/1/12 0012.
 */
public class Tools {

    public static Point getDisplaySize() {
        DisplayMetrics displayMetrics = SurfaceApplication.getContext().getResources().getDisplayMetrics();
        Point displaySize = new Point();
        displaySize.x = displayMetrics.widthPixels;
        displaySize.y = displayMetrics.heightPixels;
        return displaySize;
    }

    public static int dp2px(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(Context context, int pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void toast(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Toast.makeText(SurfaceApplication.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        return rotateBitmap(source, angle, 1f);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle, float scale) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        matrix.postScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        source.recycle();
        return result;
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w("NavigationBar", e);
        }

        return hasNavigationBar;
    }

    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    public static String getFormatNumber(float f) {
        return getFormatNumber(f, 1);
    }

    public static String getFormatNumber(float f, int decimals) {
        String strDecimals;
        switch (decimals) {
            case 1:
                strDecimals = "0.0";
                break;
            case 2:
                strDecimals = "0.00";
                break;
            case 3:
                strDecimals = "0.000";
                break;
            default:
                strDecimals = "0.0";
                break;
        }
        DecimalFormat decimalFormat = new DecimalFormat(strDecimals);//构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(f);
    }

    public static Point pointf2Point(RulerCalculator.PointO pointf) {
        if (pointf != null) {
            return new Point(pointf.x, pointf.y);
        }
        return null;
    }

    public static List<Point> pointfs2Points(List<RulerCalculator.PointO> pointfs) {
        if (pointfs != null) {
            List<Point> points = new ArrayList<>();
            for (RulerCalculator.PointO ptf : pointfs) {
                points.add(pointf2Point(ptf));
            }

            return points;
        }

        return null;
    }

    public static RulerCalculator.PointO point2PointO(Point point) {
        if (point != null) {
            return new RulerCalculator.PointO(point.x, point.y);
        }

        return null;
    }
}
