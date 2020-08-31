package com.xingchen.imageselector.utils;

import android.content.Context;

import com.xingchen.imageselector.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String getImageTime(Context context, long time) {
        if (String.valueOf(time).length() < 13) {
            time *= 1000;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Calendar imageTime = Calendar.getInstance();
        imageTime.setTimeInMillis(time);
        if (sameDay(calendar, imageTime)) {
            return context.getString(R.string.selector_this_today);
        } else if (sameWeek(calendar, imageTime)) {
            return context.getString(R.string.selector_this_week);
        } else if (sameMonth(calendar, imageTime)) {
            return context.getString(R.string.selector_this_month);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM", Locale.getDefault());
            return sdf.format(new Date(time));
        }
    }

    public static boolean sameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean sameWeek(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR);
    }

    public static boolean sameMonth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }
}
