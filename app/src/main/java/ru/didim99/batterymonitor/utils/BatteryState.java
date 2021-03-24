package ru.didim99.batterymonitor.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

/**
 * Battery state container
 * Created by didim99 on 24.07.20.
 */

public class BatteryState {
  private static final int EXTRA_DEFAULT = -1;
  private static final int BATTERY_LEVEL_LOW = 15;
  private static final int BATTERY_LEVEL_FULL = 100;
  private static final String KEY_LAST_PERCENT = "battery.lastPercent";
  private static final String KEY_LAST_STATE = "battery.lastState";
  private static final String KEY_LAST_TIME = "battery.lastTime";
  private static final String KEY_LAST_TIME_FULL = "battery.lastTime.full";
  private static final String KEY_LAST_DURATION_CHARGE = "stat.lastDuration.charge";
  private static final String KEY_LAST_DURATION_USAGE = "stat.lastDuration.usage";
  private static final int DEFAULT_LAST_PERCENT = -1;
  private static final boolean DEFAULT_LAST_STATE = false;
  private static final long DEFAULT_LAST_TIME = 0;
  private static final long DEFAULT_LAST_TIME_FULL = 0;
  private static final long DEFAULT_LAST_DURATION_CHARGE = 0;
  private static final long DEFAULT_LAST_DURATION_USAGE = 0;

  private boolean isCharging, isLow;
  private int percent, lastPercent;
  private long lastTime, lastTimeFull;
  private long lastDCharge, lastDUsage;
  private boolean prevState;

  private BatteryState(Context context, Intent batteryIntent) {
    int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, EXTRA_DEFAULT);
    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
      status == BatteryManager.BATTERY_STATUS_FULL;
    percent = getPercent(batteryIntent);
    isLow = percent <= BATTERY_LEVEL_LOW;
    loadFromSettings(context);
  }

  private void loadFromSettings(Context context) {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    lastPercent = settings.getInt(KEY_LAST_PERCENT, DEFAULT_LAST_PERCENT);
    prevState = settings.getBoolean(KEY_LAST_STATE, DEFAULT_LAST_STATE);
    lastTime = settings.getLong(KEY_LAST_TIME, DEFAULT_LAST_TIME);
    lastTimeFull = settings.getLong(KEY_LAST_TIME_FULL, DEFAULT_LAST_TIME_FULL);
    lastDCharge = settings.getLong(KEY_LAST_DURATION_CHARGE, DEFAULT_LAST_DURATION_CHARGE);
    lastDUsage = settings.getLong(KEY_LAST_DURATION_USAGE, DEFAULT_LAST_DURATION_USAGE);
  }

  public void update(Context context) {
    if (isChargedNow()) updateTimeFull(context);
    if (needUpdatePercent()) updatePercent(context);
    if (needUpdateTime()) {
      updateDuration(context);
      updateTime(context);
    }
  }

  private void updateDuration(Context context) {
    long now = System.currentTimeMillis();
    if (isCharging)
      lastDUsage = now - lastTime;
    else
      lastDCharge = now - lastTime;

    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putLong(KEY_LAST_DURATION_CHARGE, lastDCharge)
      .putLong(KEY_LAST_DURATION_USAGE, lastDUsage).apply();
  }

  private void updateTime(Context context) {
    lastTime = System.currentTimeMillis();
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putBoolean(KEY_LAST_STATE, isCharging)
      .putLong(KEY_LAST_TIME, lastTime).apply();
  }

  private void updateTimeFull(Context context) {
    lastTimeFull = System.currentTimeMillis();
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putLong(KEY_LAST_TIME_FULL, lastTimeFull).apply();
  }

  private void updatePercent(Context context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putInt(KEY_LAST_PERCENT, percent).apply();
  }

  private boolean isChargedNow() {
    return isCharging && isFull()
      && lastPercent < BATTERY_LEVEL_FULL;
  }

  private boolean needUpdatePercent() {
    return lastPercent == DEFAULT_LAST_PERCENT
      || lastPercent != percent;
  }

  private boolean needUpdateTime() {
    if (lastTime == DEFAULT_LAST_TIME)
      return true;
    if (isCharging && isFull())
      return false;
    return isStateChanged()
      || isCharging && lastPercent > percent
      || !isCharging && lastPercent < percent;
  }

  private int getPercent(Intent battery) {
    int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, EXTRA_DEFAULT);
    int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, EXTRA_DEFAULT);
    return Math.round(level / (float) scale * 100f);
  }

  public boolean isCharging() {
    return isCharging;
  }

  private boolean isFull() {
    return percent == BATTERY_LEVEL_FULL;
  }

  public boolean isLow() {
    return isLow;
  }

  private boolean isStateChanged() {
    return prevState != isCharging;
  }

  public int getPercent() {
    return percent;
  }

  public long getLifeTime() {
    long now = (isCharging && isFull()) ?
      lastTimeFull : System.currentTimeMillis();
    return (now - lastTime) / 1000;
  }

  public long getLastDCharge() {
    return lastDCharge;
  }

  public long getLastDUsage() {
    return lastDUsage;
  }

  public static BatteryState load(Context context) {
    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent battery = context.registerReceiver(null, filter);
    return battery == null ? null : new BatteryState(context, battery);
  }
}
