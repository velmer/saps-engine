package org.fogbowcloud.saps.engine.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Contains util methods for handling Date objects and its operations.
 */
public class DateUtil {

    /**
     * Creates a list of consecutive dates from {@param initDate} to {@param endDate}.
     * Both limits of interval are inclusive.
     *
     * @param initDate Initial date of interval.
     * @param endDate Last date of interval.
     * @return List of consecutive dates from {@param initDate} to {@param endDate}.
     */
    public static List<Date> getDateListFromInterval(Date initDate, Date endDate) {
        return getDateListFromInterval(calendarFromDate(initDate),
                calendarFromDate(endDate));
    }

    /**
     * Creates a list of consecutive dates from {@param initDate} to {@param endDate}.
     * Both limits of interval are inclusive.
     *
     * @param initDate Initial date of interval.
     * @param endDate Last date of interval.
     * @return List of consecutive dates from {@param initDate} to {@param endDate}.
     */
    public static List<Date> getDateListFromInterval(Calendar initDate, Calendar endDate) {
        List<Date> dates = new ArrayList<>();
        // Ensures end date is included.
        endDate.roll(Calendar.DATE, true);
        for(Calendar currentDate = initDate; currentDate.before(endDate); currentDate.roll(Calendar.DATE, true)) {
            dates.add(currentDate.getTime());
        }
        endDate.roll(Calendar.DATE, false);
        return dates;
    }

    /**
     * Creates a {@link Calendar} from specified date.
     *
     * @param date Date of calendar that will be created.
     * @return Calendar from specified date.
     */
    public static Calendar calendarFromDate(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Builds a {@link Date} object from specified parameters.
     *
     * @param year   Year of Date.
     * @param month  Month of Date.
     * @param day    Day of Date.
     * @return Date with specified parameters.
     */
    public static Date buildDate(int year, int month, int day) {
        return buildDate(year, month, day, 0, 0, 0);
    }

    /**
     * Builds a {@link Date} object from specified parameters.
     *
     * @param year   Year of Date.
     * @param month  Month of Date. 0-based (e.g. 0 for January).
     * @param day    Day of Date.
     * @param hour   Hour of Date.
     * @param minute Minute of Date.
     * @param second Second of Date.
     * @return Date with specified parameters.
     */
    public static Date buildDate(int year, int month, int day, int hour, int minute, int second) {
        return new GregorianCalendar(year, month, day, hour, minute, second).getTime();
    }
}
