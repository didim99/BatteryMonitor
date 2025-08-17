package ru.didim99.batterymonitor.core;

import androidx.annotation.NonNull;
import ru.didim99.batterymonitor.utils.TimeUtils;

/**
 * Created by didim99 on 17.08.2025.
 */

public class LevelStat {
  public static final int DEFAULT_LEVEL = -1;

  private int start; // percent
  private int end; // percent
  private long duration; // milliseconds

  public LevelStat(int start, int end, long duration) {
    this.start = start;
    this.end = end;
    this.duration = duration;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public long getDuration() {
    return duration;
  }

  public long getDurationSeconds() {
    return TimeUtils.millisToSeconds(duration);
  }

  public void setLevels(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  @NonNull
  @Override
  public String toString() {
    return "LevelStat{" +
      "start=" + start +
      ", end=" + end +
      ", duration=" + duration +
      '}';
  }
}
