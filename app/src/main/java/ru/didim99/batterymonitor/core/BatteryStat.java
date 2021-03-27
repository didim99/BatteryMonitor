package ru.didim99.batterymonitor.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ru.didim99.batterymonitor.utils.TimeUtils;

/**
 * Created by didim99 on 26.03.21.
 */

public class BatteryStat {
  private static final String KEY_LAST_PERCENT = "battery.lastPercent";
  private static final String KEY_LAST_STATE = "battery.lastState";
  private static final String KEY_LAST_TIME = "battery.lastTime";
  private static final String KEY_LAST_TIME_FULL = "battery.lastTime.full";
  private static final String KEY_LAST_DURATION_CHARGE = "stat.lastDuration.charge";
  private static final String KEY_LAST_DURATION_USAGE = "stat.lastDuration.usage";
  private static final boolean DEFAULT_LAST_STATE = false;
  private static final int DEFAULT_PERCENT = -1;
  private static final long DEFAULT_TIME = 0;
  private static final long DEFAULT_DURATION = -1;

  private final BatteryState state;
  private boolean prevState;
  private int lastPercent;
  private long lastTime, lastTimeFull;
  private long lastDCharge, lastDUsage;

  public BatteryStat(Context context, BatteryState state) {
    this.state = state;
    loadFromSettings(context);
  }

  private void loadFromSettings(Context context) {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    lastPercent = settings.getInt(KEY_LAST_PERCENT, DEFAULT_PERCENT);
    prevState = settings.getBoolean(KEY_LAST_STATE, DEFAULT_LAST_STATE);
    lastTime = settings.getLong(KEY_LAST_TIME, DEFAULT_TIME);
    lastTimeFull = settings.getLong(KEY_LAST_TIME_FULL, DEFAULT_TIME);
    lastDCharge = settings.getLong(KEY_LAST_DURATION_CHARGE, DEFAULT_DURATION);
    lastDUsage = settings.getLong(KEY_LAST_DURATION_USAGE, DEFAULT_DURATION);
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
    if (state.isCharging())
      lastDUsage = now - lastTime;
    else
      lastDCharge = (state.isFull() ? lastTimeFull : now) - lastTime;

    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putLong(KEY_LAST_DURATION_CHARGE, lastDCharge)
      .putLong(KEY_LAST_DURATION_USAGE, lastDUsage).apply();
  }

  private void updateTime(Context context) {
    lastTime = System.currentTimeMillis();
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putBoolean(KEY_LAST_STATE, state.isCharging())
      .putLong(KEY_LAST_TIME, lastTime).apply();
  }

  private void updateTimeFull(Context context) {
    lastTimeFull = System.currentTimeMillis();
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putLong(KEY_LAST_TIME_FULL, lastTimeFull).apply();
  }

  private void updatePercent(Context context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putInt(KEY_LAST_PERCENT, state.getPercent()).apply();
  }

  private boolean isChargedNow() {
    return state.isCharging() && state.isFull()
      && lastPercent < BatteryState.BATTERY_LEVEL_FULL;
  }

  private boolean needUpdatePercent() {
    return lastPercent == DEFAULT_PERCENT
      || lastPercent != state.getPercent();
  }

  private boolean needUpdateTime() {
    if (lastTime == DEFAULT_TIME)
      return true;
    if (state.isCharging() && state.isFull())
      return false;
    return isStateChanged()
      || state.isCharging() && lastPercent > state.getPercent()
      || !state.isCharging() && lastPercent < state.getPercent();
  }

  public long getLifeTime() {
    long now = (state.isCharging() && state.isFull()) ?
      lastTimeFull : System.currentTimeMillis();
    return TimeUtils.millisToSeconds(now - lastTime);
  }

  public long getLastDCharge() {
    return TimeUtils.millisToSeconds(lastDCharge);
  }

  public long getLastDUsage() {
    return TimeUtils.millisToSeconds(lastDUsage);
  }

  public BatteryState getCurrentState() {
    return state;
  }

  private boolean isStateChanged() {
    return prevState != state.isCharging();
  }
}
