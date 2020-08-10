package ru.didim99.batterymonitor.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import ru.didim99.batterymonitor.R;
import ru.didim99.batterymonitor.utils.BatteryState;
import ru.didim99.batterymonitor.utils.ColorScale;

/**
 * Created by didim99 on 10.08.20.
 */

class WidgetDrawer {
  private static final String FONT_PATH = "fonts/LCDNova.ttf";
  private static final double BATTERY_ZERO_LEVEL = 0.0;
  private static final double BATTERY_HALF_LEVEL = 0.5;
  private static final double BATTERY_FULL_LEVEL = 1.0;
  private static final int CANVAS_WIDTH = 400;
  private static final int CANVAS_HEIGHT = 800;
  private static final int FONT_SIZE_MAIN = 150;
  private static final int FONT_SIZE_TIME = 85;
  private static final int BATT_PIN_PART = 8;
  private static final int TEXT_SHADOW_OFFSET = 8;
  private static final int FONT_SIZE_SIGN = FONT_SIZE_MAIN / 2;
  private static final int SIGN_MARGIN = FONT_SIZE_SIGN / 4;
  private static final int BATT_CORNER_RADIUS = CANVAS_WIDTH / 20;
  private static final int BATT_PIN_WIDTH = CANVAS_WIDTH / 2;
  private static final int BATT_PIN_HEIGHT = CANVAS_HEIGHT * BATT_PIN_PART / 100;

  private Context context;
  private Resources res;
  private Canvas canvas;
  private Paint paint;

  private int textColor;
  private int textShadowColor;

  WidgetDrawer(Context context) {
    this.res = context.getResources();
    this.context = context;
  }

  public Bitmap draw(BatteryState state) {
    Bitmap bitmap = Bitmap.createBitmap(CANVAS_WIDTH,
      CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);
    paint = getPaint(context);

    getTextColors(state);
    drawBackground(state.getPercent());
    drawText(state);
    return bitmap;
  }

  private void drawBackground(int percent) {
    int bgColor = res.getColor(R.color.widgetBackground);
    ColorScale colorScale = new ColorScale(
      new ColorScale.Point(BATTERY_ZERO_LEVEL, res.getColor(R.color.batteryZero)),
      new ColorScale.Point(BATTERY_HALF_LEVEL, res.getColor(R.color.batteryHalf)),
      new ColorScale.Point(BATTERY_FULL_LEVEL, res.getColor(R.color.batteryFull)));

    RectF rectPin = new RectF(
      (CANVAS_WIDTH - BATT_PIN_WIDTH) / 2f, 0,
      (CANVAS_WIDTH + BATT_PIN_WIDTH) / 2f, BATT_PIN_HEIGHT);
    RectF rectBody = new RectF(
      0, BATT_PIN_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT);

    paint.setColor(bgColor);
    canvas.drawRect(rectPin, paint);
    canvas.drawRoundRect(rectBody,
      BATT_CORNER_RADIUS, BATT_CORNER_RADIUS, paint);

    int maxBody = 100 - BATT_PIN_PART;
    if (percent > maxBody) {
      rectPin.top = BATT_PIN_HEIGHT - BATT_PIN_HEIGHT
        * (percent - maxBody) / (float) BATT_PIN_PART;
    } else {
      rectBody.top = CANVAS_HEIGHT
        - (CANVAS_HEIGHT - BATT_PIN_HEIGHT)
        * (percent / (float) maxBody);
    }

    paint.setColor(colorScale.get(percent / 100.0));
    if (percent > maxBody)
      canvas.drawRect(rectPin, paint);
    canvas.drawRoundRect(rectBody,
      BATT_CORNER_RADIUS, BATT_CORNER_RADIUS, paint);
  }

  private void drawText(BatteryState state) {
    String percentSign = res.getString(R.string.percentSign);
    String percentStr = String.valueOf(state.getPercent());
    String timeStr = getTimeString(state.getLifeTime());

    // Percent string
    int numWidth = FONT_SIZE_MAIN / 2 * percentStr.length();
    int textWidth = numWidth + SIGN_MARGIN + FONT_SIZE_SIGN;
    int numPosX = (CANVAS_WIDTH - textWidth) / 2;
    int signPosX = numPosX + numWidth + SIGN_MARGIN;
    int textPosY = CANVAS_HEIGHT - (FONT_SIZE_MAIN + SIGN_MARGIN);

    paint.setTextAlign(Paint.Align.LEFT);
    drawShadowedText(percentStr, FONT_SIZE_MAIN, numPosX, textPosY);
    drawShadowedText(percentSign, FONT_SIZE_SIGN, signPosX, textPosY);

    // Time string
    int timePosX = CANVAS_WIDTH / 2;
    int timePosY = textPosY + FONT_SIZE_TIME + SIGN_MARGIN * 2;

    paint.setTextAlign(Paint.Align.CENTER);
    drawShadowedText(timeStr, FONT_SIZE_TIME, timePosX, timePosY);
  }

  private void drawShadowedText(String text, int fontSize, float posX, float posY) {
    paint.setTextSize(fontSize);
    paint.setColor(textShadowColor);
    canvas.drawText(text, posX + TEXT_SHADOW_OFFSET,
      posY + TEXT_SHADOW_OFFSET, paint);
    paint.setColor(textColor);
    canvas.drawText(text, posX, posY, paint);
  }

  private Paint getPaint(Context context) {
    Typeface tf = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
    Paint paint = new Paint();
    paint.setSubpixelText(true);
    paint.setAntiAlias(true);
    paint.setTypeface(tf);
    return paint;
  }

  private void getTextColors(BatteryState state) {
    if (state.isCharging()) textColor = res.getColor(R.color.widgetTextCharging);
    else if (state.isLow()) textColor = res.getColor(R.color.widgetTextLow);
    else textColor = res.getColor(R.color.widgetTextNormal);
    textShadowColor = res.getColor(R.color.widgetTextShadow);
  }

  private String getTimeString(long time) {
    if (time < 60) return res.getString(R.string.time_seconds, time);
    long seconds = time % 60; time /= 60;
    if (time < 60) return res.getString(R.string.time_minutes, time, seconds);
    long minutes = time % 60; time /= 60;
    if (time < 24) return res.getString(R.string.time_hours, time, minutes);
    long hours = time % 24; time /= 24;
    return res.getString(R.string.time_days, time, hours);
  }
}
