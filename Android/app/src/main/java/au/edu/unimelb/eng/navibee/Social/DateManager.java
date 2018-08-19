package au.edu.unimelb.eng.navibee.Social;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateManager {
    public static String DateformatTime(Date date) {
        long time = date.getTime();
        if (isThisYear(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            if (isToday(date)) {
                int minute = minutesAgo(time);
                if (minute < 60) {
                    if (minute <= 1) {
                        return "just now";
                    } else {
                        return minute + " mins ago";
                    }
                } else {
                    return simpleDateFormat.format(date);
                }
            } else {
                if (isYestYesterday(date)) {
                    return "Yesterday " + simpleDateFormat.format(date);
                } else if (isThisWeek(date)) {
                    String weekday = null;
                    if (date.getDay() == 1) {
                        weekday = "Monday";
                    }
                    if (date.getDay() == 2) {
                        weekday = "Tuesday";
                    }
                    if (date.getDay() == 3) {
                        weekday = "Wednesday";
                    }
                    if (date.getDay() == 4) {
                        weekday = "Thursday";
                    }
                    if (date.getDay() == 5) {
                        weekday = "Friday";
                    }
                    if (date.getDay() == 6) {
                        weekday = "Saturday";
                    }
                    if (date.getDay() == 0) {
                        weekday = "Sunday";
                    }
                    return weekday + " " + simpleDateFormat.format(date);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                    return sdf.format(date);
                }
            }
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
    }

    private static int minutesAgo(long time) {
        return (int) ((System.currentTimeMillis() - time) / (60000));
    }

    private static boolean isToday(Date date) {
        Date now = new Date();
        return (date.getYear() == now.getYear()) && (date.getMonth() == now.getMonth()) && (date.getDate() == now.getDate());
    }

    private static boolean isYestYesterday(Date date) {
        Date now = new Date();
        return (date.getYear() == now.getYear()) && (date.getMonth() == now.getMonth()) && (date.getDate() + 1 == now.getDate());
    }

    private static boolean isThisWeek(Date date) {
        Date now = new Date();
        if ((date.getYear() == now.getYear()) && (date.getMonth() == now.getMonth())) {
            if (now.getDay() - date.getDay() < now.getDay() && now.getDate() - date.getDate() > 0 && now.getDate() - date.getDate() < 7) {
                return true;
            }
        }
        return false;
    }

    private static boolean isThisYear(Date date) {
        Date now = new Date();
        return date.getYear() == now.getYear();
    }
}
