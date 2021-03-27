package ru.didim99.batterymonitor.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Multi-stop color scale with linear interpolation between stops
 * Created by didim99 on 09.02.20.
 */
public class ColorScale {
  private static final double MIN_VALUE = 0.0;
  private static final double MAX_VALUE = 1.0;

  private final ArrayList<Point> table;

  public ColorScale(Point... points) {
    if (points == null)
      throw new IllegalArgumentException("Color table not defined");
    if (points.length < 2)
      throw new IllegalArgumentException("Required minimum 2 color stops");
    if (points[0].value != MIN_VALUE)
      throw new IllegalArgumentException("First color stop value must be " + MIN_VALUE);
    if (points[points.length - 1].value != MAX_VALUE)
      throw new IllegalArgumentException("Last color stop value must be " + MAX_VALUE);

    this.table = new ArrayList<>();
    table.addAll(Arrays.asList(points));
    Collections.sort(table);
  }

  public int get(double value) {
    value = bound(value, MIN_VALUE, MAX_VALUE);

    Point prev = table.get(0);
    for (int i = 1; i < table.size(); i++) {
      Point next = table.get(i);
      if (value < next.value)
        return lerp(prev.color, next.color,
          norm(value, prev.value, next.value));
      if (value == next.value)
        return next.color;
      prev = next;
    }

    return table.get(table.size() - 1).color;
  }

  public static class Point implements Comparable<Point> {
    private final double value;
    private final int color;

    public Point(double value, int color) {
      this.value = bound(value, MIN_VALUE, MAX_VALUE);
      this.color = color;
    }

    @Override
    public int compareTo(Point o) {
      return Double.compare(value, o.value);
    }
  }

  /* ======== MATH UTILS ======== */

  private static double bound(double v, double min, double max) {
    return Math.min(Math.max(v, min), max);
  }

  private static double norm(double v, double min, double max) {
    return (v - min) / (max - min);
  }

  /* ======== COLOR UTILS ======== */

  private static int lerp(int bg, int fg, double alpha) {
    double gamma = 1 - alpha;
    int res = (int) ((bg & 0xff) * gamma + (fg & 0xff) * alpha);
    res |= (int) (((bg >> 8) & 0xff) * gamma + ((fg >> 8) & 0xff) * alpha) << 8;
    res |= (int) (((bg >> 16) & 0xff) * gamma + ((fg >> 16) & 0xff) * alpha) << 16;
    res |= 0xff000000;
    return res;
  }
}
