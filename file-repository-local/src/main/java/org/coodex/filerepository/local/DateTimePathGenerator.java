package org.coodex.filerepository.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * separate by date time
 * UTC+8
 */
public class DateTimePathGenerator implements IPathGenerator {
    private static Logger log = LoggerFactory.getLogger(DateTimePathGenerator.class);

    /**
     * get a file path separate by date time
     * @param seed  parameter for generator
     * @return      a file path like "YYYY/MM/dd/HH/mm/"
     */
    @Override
    public String getPath(String seed) {
        UUID uuid = toUUID(seed);
        Instant instant = UuidHelper.getInstantFromUUID(uuid);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeInMillis(instant.toEpochMilli());
        StringBuilder path = new StringBuilder();
        path.append(calendar.get(Calendar.YEAR)).append(File.separatorChar);
        int month = calendar.get(Calendar.MONTH) + 1;
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

    private UUID toUUID(String seed) {
        int index = seed.indexOf('$');
        String uuidStr = index == -1 ? seed : seed.substring(index + 1);
        if (uuidStr.length() == 32) {
            StringBuilder sb = new StringBuilder();
            sb.append(uuidStr.substring(0, 8)).append('-').append(uuidStr.substring(8, 12)).append('-')
                    .append(uuidStr.substring(12, 16)).append('-').append(uuidStr.substring(16, 20)).append('-')
                    .append(uuidStr.substring(20));
            uuidStr = sb.toString();
        }
        if (uuidStr.length() == 36) {
            return UUID.fromString(uuidStr);
        } else {
            log.warn("Illegal time-based UUID string: {}", seed);
            throw new RuntimeException("Illegal time-based UUID string " + seed);
        }
    }
}
