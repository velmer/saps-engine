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

    public static Calendar calendarFromDate(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }
}
