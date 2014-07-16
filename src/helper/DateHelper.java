package helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.text.TextUtils;
import android.text.format.DateFormat;

public class DateHelper {
//    public static SimpleDateFormat MOMENT_ITEM_DATE = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
//    public static SimpleDateFormat DURATION_TIME = new SimpleDateFormat("HH:mm:ss.SSS");
    
	/**
	 * 将calendar设置为当日的午夜，如2008-8-8 12:00 -> 2008-8-8 0:00（将修改原calendar）
	 * @param calendar
	 * @return
	 */
	public static Calendar clearTimePart(Calendar calendar) {
		if (calendar != null) {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		
		return calendar;
	}
	
	/**
	 * 获得calendar指定那天的午夜（获得新的calendar实例）
	 * @param calendar
	 * @return
	 */
	public static Calendar midnight(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		
		return clearTimePart((Calendar)calendar.clone());
	}
	
	/**
	 * 两个时间相差的天数（原calendar不受影响）
	 * @param date1
	 * @param date2
	 * @return 返回date2-date1，如果date1或date2为空返回0
	 */
	public static int daysBetween(Calendar date1, Calendar date2) {
		if (date1 == null || date2 == null) {
			return 0;
		}

		return (int) ((midnight(date2).getTimeInMillis() - midnight(date1).getTimeInMillis()) / (24 * 3600000L));
	}

	/**
	 * 指定时间距离今天的天数（now - date）
	 * @param date
	 * @return now - date，如果date如null，返回0
	 */
	public static int daysFromNow(Date date) {
		if (date == null) {
			return 0;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return daysBetween(calendar, Calendar.getInstance());
	}
	
	/**
	 * 将W3C格式(yyyy-MM-dd'T'HH:mm:ssZ)的时间字符串转换成时间
	 * @param string
	 * @return Date，如果string为空或格式不正确，返回null
	 */
	public static Date parseW3C(String string) {
		if (TextUtils.isEmpty(string)) {
			return null;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			return dateFormat.parse(string);
		} catch (java.text.ParseException e) { }

		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		try {
			return dateFormat.parse(string);
		} catch (java.text.ParseException e) {
			return null;
		}
	}

	/**
	 * 将date转换成W3C格式字符串
	 * @param date
	 * @return 字符串，如果date为空返回null
	 */
	public static String toW3C(Date date) {
		if (date == null) {
			return null;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dateFormat.format(date);
	}
	
	/*
	 * 根据当前时间返回时间的描述语
	 */
	public static String prettifyDate(Date date) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date);
		
		return (String) DateFormat.format(getFormatString(calendar1, Calendar.getInstance()), date);
	}

	// relative can't be null
	private static String getFormatString(Calendar date, Calendar relative) {
		if (date.get(Calendar.YEAR) != relative.get(Calendar.YEAR)) {
			// 不是同一年，显示完整时间
			return "yyyy-MM-dd";
		} else {
			if (date.get(Calendar.DAY_OF_YEAR) == relative.get(Calendar.DAY_OF_YEAR)) {
				return "kk:mm";
			} else {
				return "MM-dd";
			}
		}
	}

    /**
     * 转换02:34:00.33的时间为long 毫秒
     * @param time
     * @return
     */
    public static long getMillSecondFormHMC(String time) {
        if (!TextUtils.isEmpty(time)) {
            // 不是同一年，显示完整时间
            long duration = -1;
            String[] array = time.split("[:,.]");
            try {
                duration = (Integer.valueOf(array[0])*3600 + Integer.valueOf(array[1])*60 + Integer.valueOf(array[2]))*1000 + Integer.valueOf(array[3]);
            } catch (Exception e) {

            }
            return duration * 1000;
        } else {
            return -1;
        }
    }
}
