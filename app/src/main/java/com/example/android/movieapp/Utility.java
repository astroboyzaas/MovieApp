package com.example.android.movieapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    public static void setMoviesOrder(Context context, String orderValue){
        String orderSelected=context.getString(R.string.pref_order_selected);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(orderSelected, orderValue);
        editor.commit();
    }

    public static String getMoviesOrder(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String orderSelected=prefs.getString(context.getString(R.string.pref_order_selected),
                context.getString(R.string.pref_order_default));
        return orderSelected;
    }

    public static String getFriendlyYearString(String dateString){
        // returns only year
        return dateString.substring(0,4);
    }

    public static String getFriendlyDateString(String dateString){
        // returns dd-MM-yyyy
        StringBuffer buffer = new StringBuffer();
        buffer.append(dateString.substring(8)+"-");
        buffer.append(dateString.substring(5,7)+"-");
        buffer.append(dateString.substring(0,4));
        return buffer.toString();
    }
}
