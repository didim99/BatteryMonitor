package ru.didim99.batterymonitor.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Battery state container
 * Created by didim99 on 24.07.20.
 */

public class BatteryState {
  private static final int EXTRA_DEFAULT = -1;
  private static final int BATTERY_LEVEL_LOW = 15;
  public static final int BATTERY_LEVEL_FULL = 100;

  private final boolean isCharging, isLow;
  private final int percent;

  private BatteryState(Intent batteryIntent) {
    int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, EXTRA_DEFAULT);
    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
      status == BatteryManager.BATTERY_STATUS_FULL;
    percent = getPercent(batteryIntent);
    isLow = percent <= BATTERY_LEVEL_LOW;
  }

  private int getPercent(Intent battery) {
    int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, EXTRA_DEFAULT);
    int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, EXTRA_DEFAULT);
    return Math.round(level / (float) scale * 100f);
  }

  public boolean isCharging() {
    return isCharging;
  }

  public boolean isFull() {
    return percent == BATTERY_LEVEL_FULL;
  }

  public boolean isLow() {
    return isLow;
  }

  public int getPercent() {
    return percent;
  }

  public static BatteryState load(Context context) {
    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent battery = context.registerReceiver(null, filter);
    return battery == null ? null : new BatteryState(battery);
  }
}
