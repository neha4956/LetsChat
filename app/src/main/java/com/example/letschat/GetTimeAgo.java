package com.example.letschat;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GetTimeAgo extends Application {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else if(diff < 365*DAY_MILLIS) {
            SimpleDateFormat sdf3 = new SimpleDateFormat("dd MMM", Locale.getDefault());              //  03:12 am (whatsApp uses this)
            java.util.Date currenTimeZone = new java.util.Date((long) time );
            String ans= sdf3.format(currenTimeZone);
            return ans;
        }else{
            SimpleDateFormat sdf3 = new SimpleDateFormat("dd MMM YYYY", Locale.getDefault());              //  03:12 am (whatsApp uses this)
            java.util.Date currenTimeZone = new java.util.Date((long) time );
            String ans= sdf3.format(currenTimeZone);
            return ans;
        }
    }
}
