package com.motopit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by abdul on 10/7/15.
 */
public class Utility {
    private static Pattern pattern;
    private static Matcher matcher;
    private static final String PHONE_PATTERN = "[7-9]{1}[0-9]{9}";
    private static final String ADDR_PATTERN = "[a-zA-Z][a-zA-Z ]{2,20}";
    private static final String NAME_PATTERN = "^[a-zA-Z]{1}[a-zA-Z., ]{2,20}$";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PLATE_PATTERN = "[A-Z][A-Z][0-9][0-9][A-Z0-9][A-Z0-9][0-9][0-9][0-9][0-9]";



    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }

    public static String isNull(String value){
        return value!=null&&!value.equals("null")? value:"";
    }

    public static boolean isNetworkAvailable(Context context) {
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI};
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null &&
                        activeNetworkInfo.getType() == networkType)
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


}

