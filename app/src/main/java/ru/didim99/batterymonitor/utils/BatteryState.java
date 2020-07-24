package ru.didim99.batterymonitor.utils;

import android.content.Intent;
import android.os.BatteryManager;

/**
 * Battery state container
 * Created by didim99 on 24.07.20.
 */

public class BatteryState {
  private static final int DEFAULT_VALUE = -1;
  private static final int BATTERY_LOW_LEVEL = 15;

  private boolean isCharging, isLow;
  private int percent;

  public BatteryState(Intent batteryIntent) {
    int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, DEFAULT_VALUE);
    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
      status == BatteryManager.BATTERY_STATUS_FULL;
    isLow = percent <= BATTERY_LOW_LEVEL;
    percent = getPercent(batteryIntent);
  }

  private int getPercent(Intent battery) {
    int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, DEFAULT_VALUE);
    int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_VALUE);
    return Math.round(level / (float) scale * 100f);
  }

  public boolean isCharging() {
    return isCharging;
  }

  public boolean isLow() {
    return isLow;
  }

  public int getPercent() {
    return percent;
  }
}
