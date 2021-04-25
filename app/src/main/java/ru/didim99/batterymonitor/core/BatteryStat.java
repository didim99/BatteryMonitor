package ru.didim99.batterymonitor.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ru.didim99.batterymonitor.utils.TimeUtils;

/**
 * Created by didim99 on 26.03.21.
 */

public class BatteryStat {
  private static final String KEY_LAST_UPDATE = "global.lastUpdateTime";
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
  private long lastUpdate;

  public BatteryStat(Context context, BatteryState state) {
    this.state = state;
    loadFromSettings(context);
  }

  private void loadFromSettings(Context context) {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    lastUpdate = settings.getLong(KEY_LAST_UPDATE, DEFAULT_TIME);
    lastPercent = settings.getInt(KEY_LAST_PERCENT, DEFAULT_PERCENT);
    prevState = settings.getBoolean(KEY_LAST_STATE, DEFAULT_LAST_STATE);
    lastTime = settings.getLong(KEY_LAST_TIME, DEFAULT_TIME);
    lastTimeFull = settings.getLong(KEY_LAST_TIME_FULL, DEFAULT_TIME);
    lastDCharge = settings.getLong(KEY_LAST_DURATION_CHARGE, DEFAULT_DURATION);
    lastDUsage = settings.getLong(KEY_LAST_DURATION_USAGE, DEFAULT_DURATION);
  }

  public void update(Context context) {
    SharedPreferences.Editor editor = PreferenceManager
      .getDefaultSharedPreferences(context).edit();

    if (isChargedNow()) updateTimeFull(editor);
    if (needUpdatePercent()) updatePercent(editor);
    if (needUpdateTime()) {
      updateDuration(editor);
      updateTime(editor);
    }

    lastUpdate = System.currentTimeMillis();
    editor.putLong(KEY_LAST_UPDATE, lastUpdate).apply();
  }

  private void updateDuration(SharedPreferences.Editor editor) {
    long now = System.currentTimeMillis();
    if (state.isCharging())
      lastDUsage = now - lastTime;
    else
      lastDCharge = (state.isFull() ? lastTimeFull : now) - lastTime;

    editor.putLong(KEY_LAST_DURATION_CHARGE, lastDCharge)
      .putLong(KEY_LAST_DURATION_USAGE, lastDUsage);
  }

  private void updateTime(SharedPreferences.Editor editor) {
    lastTime = System.currentTimeMillis();
    editor.putBoolean(KEY_LAST_STATE, state.isCharging())
      .putLong(KEY_LAST_TIME, lastTime);
  }

  private void updateTimeFull(SharedPreferences.Editor editor) {
    lastTimeFull = System.currentTimeMillis();
    editor.putLong(KEY_LAST_TIME_FULL, lastTimeFull);
  }

  private void updatePercent(SharedPreferences.Editor editor) {
    editor.putInt(KEY_LAST_PERCENT, state.getPercent());
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
