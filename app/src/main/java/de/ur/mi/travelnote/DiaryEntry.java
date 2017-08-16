package de.ur.mi.travelnote;

import android.icu.text.DateFormat;
import android.icu.util.GregorianCalendar;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Locale;


/**
 * Created by wexle on 16.08.2017.
 */

public class DiaryEntry {
    private String body;
    private GregorianCalendar cal;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public DiaryEntry(String body, int day, int month, int year) {
        this.body = body;
        cal = new GregorianCalendar(year, month, day);
    }

    public String getBody() {
        return body;
    }

    public String getFormattedDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
            return df.format(cal.getTime());
        }
        return null;
    }

}
