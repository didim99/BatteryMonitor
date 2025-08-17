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
  private static final String KEY_LAST_LVL_CHANGE = "stat.lastLevel.stageChange";
  private static final String KEY_LAST_DURATION_CHARGE = "stat.lastDuration.charge";
  private static final String KEY_LAST_DURATION_USAGE = "stat.lastDuration.usage";
  private static final String KEY_LAST_CHG_LVL_START = "stat.lastCharge.levelStart";
  private static final String KEY_LAST_CHG_LVL_END = "stat.lastCharge.levelEnd";
  private static final String KEY_LAST_USG_LVL_START = "stat.lastUsage.levelStart";
  private static final String KEY_LAST_USG_LVL_END = "stat.lastUsage.levelEnd";
  private static final boolean DEFAULT_LAST_STATE = false;
  private static final int DEFAULT_PERCENT = LevelStat.DEFAULT_LEVEL;
  private static final long DEFAULT_TIME = 0;
  private static final long DEFAULT_DURATION = -1;

  private final BatteryState state;
  private boolean prevState;
  private int lastPercent, lastChangeLevel;
  private long lastTime, lastTimeFull;
  private LevelStat lastChgStat, lastUsgStat;
  private long lastUpdate;

  public BatteryStat(Context context, BatteryState state) {
    this.state = state;
    loadFromSettings(context);
  }

  private void loadFromSettings(Context context) {
    SharedPreferences settings = PreferenceManager
      .getDefaultSharedPreferences(context);
    lastUpdate = settings.getLong(KEY_LAST_UPDATE, DEFAULT_TIME);
    lastPercent = settings.getInt(KEY_LAST_PERCENT, DEFAULT_PERCENT);
    prevState = settings.getBoolean(KEY_LAST_STATE, DEFAULT_LAST_STATE);
    lastTime = settings.getLong(KEY_LAST_TIME, DEFAULT_TIME);
    lastTimeFull = settings.getLong(KEY_LAST_TIME_FULL, DEFAULT_TIME);
    lastChangeLevel = settings.getInt(KEY_LAST_LVL_CHANGE, DEFAULT_PERCENT);
    lastChgStat = new LevelStat(
      settings.getInt(KEY_LAST_CHG_LVL_START, DEFAULT_PERCENT),
      settings.getInt(KEY_LAST_CHG_LVL_END, DEFAULT_PERCENT),
      settings.getLong(KEY_LAST_DURATION_CHARGE, DEFAULT_DURATION)
    );
    lastUsgStat = new LevelStat(
      settings.getInt(KEY_LAST_USG_LVL_START, DEFAULT_PERCENT),
      settings.getInt(KEY_LAST_USG_LVL_END, DEFAULT_PERCENT),
      settings.getLong(KEY_LAST_DURATION_USAGE, DEFAULT_DURATION)
    );
  }

  public void update(Context context) {
    SharedPreferences.Editor editor = PreferenceManager
      .getDefaultSharedPreferences(context).edit();

    if (isChargingStateChanged()) updateChgUsgStats(editor);
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
      lastUsgStat.setDuration(now - lastTime);
    else {
      long lastFull = state.isFull() ? lastTimeFull : now;
      lastChgStat.setDuration(lastFull - lastTime);
    }

    editor.putLong(KEY_LAST_DURATION_CHARGE, lastChgStat.getDuration())
      .putLong(KEY_LAST_DURATION_USAGE, lastUsgStat.getDuration());
  }

  private void updateChgUsgStats(SharedPreferences.Editor editor) {
    int percent = state.getPercent();

    if (state.isCharging()) {
      // Assuming charge start
      lastUsgStat.setLevels(lastChangeLevel, percent);
      editor.putInt(KEY_LAST_USG_LVL_START, lastUsgStat.getStart())
        .putInt(KEY_LAST_USG_LVL_END, lastUsgStat.getEnd());
    } else {
      // Assuming charge end
      lastChgStat.setLevels(lastChangeLevel, percent);
      editor.putInt(KEY_LAST_CHG_LVL_START, lastChgStat.getStart())
        .putInt(KEY_LAST_CHG_LVL_END, lastChgStat.getEnd());
    }

    lastChangeLevel = percent;
    editor.putInt(KEY_LAST_LVL_CHANGE, lastChangeLevel);
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
    return isStateChanged();
  }

  public long getLifeTime() {
    long now = (state.isCharging() && state.isFull()) ?
      lastTimeFull : System.currentTimeMillis();
    return TimeUtils.millisToSeconds(now - lastTime);
  }

  public LevelStat getLastChgStat() {
    return lastChgStat;
  }

  public LevelStat getLastUsgStat() {
    return lastUsgStat;
  }

  public BatteryState getCurrentState() {
    return state;
  }

  private boolean isChargingStateChanged() {
    return prevState != state.isCharging();
  }

  private boolean isStateChanged() {
    // Charging state actually changed "on-line".
    return isChargingStateChanged()
      // Not charging now, but actual percent greater than previous one,
      // Assuming device was charged in powered off state.
      || state.isCharging() && lastPercent > state.getPercent()
      // Charging now, but actual percent lower than previous one,
      // Assuming device was powered off during charging, then stored
      // for long time, then connected to charger again and powered on.
      || !state.isCharging() && lastPercent < state.getPercent();
  }
}
