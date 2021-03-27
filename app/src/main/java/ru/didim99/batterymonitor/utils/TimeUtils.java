package ru.didim99.batterymonitor.utils;

import android.content.res.Resources;
import ru.didim99.batterymonitor.R;

/**
 * Created by didim99 on 30.10.20.
 */

public class TimeUtils {
  public static String getTimeString(Resources res, long time) {
    if (time < 60) return res.getString(R.string.time_seconds, time);
    long seconds = time % 60; time /= 60;
    if (time < 60) return res.getString(R.string.time_minutes, time, seconds);
    long minutes = time % 60; time /= 60;
    if (time < 24) return res.getString(R.string.time_hours, time, minutes);
    long hours = time % 24; time /= 24;
    return res.getString(R.string.time_days, time, hours);
  }

  public static String getDetailedTimeString(Resources res, long time) {
    long seconds = time % 60; time /= 60;
    if (time < 60) return res.getString(
      R.string.time_detailed_minutes, time, seconds);
    long minutes = time % 60; time /= 60;
    if (time < 24) return res.getString(
      R.string.time_detailed_hours, time, minutes, seconds);
    long hours = time % 24; time /= 24;
    return res.getString(
      R.string.time_detailed_days, time, hours, minutes, seconds);
  }

  public static long millisToSeconds(long millis) {
    return millis < 0 ? millis : millis / 1000;
  }
}
