package org.coodex.filerepository.local;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

/**
 * separate by date time
 */
public class DateTimePathGenerator implements IPathGenerator {
    /**
     * get a file path separate by date time
     * @param seed  parameter for generator
     * @return      a file path like "YYYY/MM/dd/HH/mm/"
     */
    @Override
    public String getPath(String seed) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        StringBuilder path = new StringBuilder();
        path.append(calendar.get(Calendar.YEAR)).append(File.separatorChar);
        int month = calendar.get(Calendar.MONTH);
        if (month < 10) {
            path.append("0");
        }
        path.append(month).append(File.separatorChar);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            path.append("0");
        }
        path.append(day).append(File.separatorChar);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            path.append("0");
        }
        path.append(hour).append(File.separatorChar);
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 10) {
            path.append("0");
        }
        path.append(minute).append(File.separatorChar);
        return path.toString();
    }
}
