package ru.didim99.batterymonitor.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.widget.RemoteViews;
import ru.didim99.batterymonitor.R;
import ru.didim99.batterymonitor.utils.BatteryState;
import ru.didim99.batterymonitor.utils.ColorScale;

/**
 * Created by didim99 on 06.07.19.
 */
public class WidgetProvider extends AppWidgetProvider {
  private static final String FONT_PATH = "fonts/LCDNova.ttf";
  private static final int UPDATE_PERIOD = 60000;
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

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    String action = intent.getAction();
    if (Intent.ACTION_POWER_CONNECTED.equals(action)
      || Intent.ACTION_POWER_DISCONNECTED.equals(action)
      || Intent.ACTION_BATTERY_LOW.equals(action)
      || Intent.ACTION_BATTERY_OKAY.equals(action)) {
      AppWidgetManager manager = AppWidgetManager.getInstance(context);
      int[] ids = manager.getAppWidgetIds(
        new ComponentName(context, WidgetProvider.class));
      context.sendBroadcast(getUpdateIntent(ids));
    }
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
    BatteryState state = BatteryState.load(context);
    if (state == null) return;

    Bitmap bitmap = drawWidget(context, state);
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
    views.setImageViewBitmap(R.id.ivBackground, bitmap);
    manager.updateAppWidget(appWidgetIds, views);
    setupSelfUpdate(context, appWidgetIds);
  }

  private Intent getUpdateIntent(int[] ids) {
    Intent intent = new Intent();
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    return intent;
  }

  private void setupSelfUpdate(Context context, int[] ids) {
    Intent updateIntent = getUpdateIntent(ids);
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC, System.currentTimeMillis() + UPDATE_PERIOD,
      PendingIntent.getBroadcast(context, 0, updateIntent, 0));
  }

  private Bitmap drawWidget(Context context, BatteryState state) {
    Bitmap bitmap = Bitmap.createBitmap(CANVAS_WIDTH,
      CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
    Resources res = context.getResources();
    Canvas canvas = new Canvas(bitmap);
    Paint paint = getPaint(context);

    drawBackground(res, canvas, paint, state.getPercent());
    drawText(res, canvas, paint, state);
    return bitmap;
  }

  private Paint getPaint(Context context) {
    Typeface tf = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
    Paint paint = new Paint();
    paint.setSubpixelText(true);
    paint.setAntiAlias(true);
    paint.setTypeface(tf);
    return paint;
  }

  private void drawBackground(Resources res, Canvas canvas, Paint paint, int percent) {
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

  private void drawText(Resources res, Canvas canvas, Paint paint, BatteryState state) {
    int textColor;
    if (state.isCharging()) textColor = res.getColor(R.color.widgetTextCharging);
    else if (state.isLow()) textColor = res.getColor(R.color.widgetTextLow);
    else textColor = res.getColor(R.color.widgetTextNormal);
    int textShadowColor = res.getColor(R.color.widgetTextShadow);
    String percentSign = res.getString(R.string.percentSign);
    String percentStr = String.valueOf(state.getPercent());
    String timeStr = getTimeString(res, state.getLifeTime());

    // Percent string position
    int numWidth = FONT_SIZE_MAIN / 2 * percentStr.length();
    int textWidth = numWidth + SIGN_MARGIN + FONT_SIZE_SIGN;
    int numPosX = (CANVAS_WIDTH - textWidth) / 2;
    int signPosX = numPosX + numWidth + SIGN_MARGIN;
    int textPosY = CANVAS_HEIGHT - (FONT_SIZE_MAIN + SIGN_MARGIN);
    // Time string position
    int timeWidth = FONT_SIZE_TIME / 2 * timeStr.length();
    int timePosX = (CANVAS_WIDTH - timeWidth) / 2;
    int timePosY = textPosY + FONT_SIZE_TIME + SIGN_MARGIN * 2;

    paint.setTextSize(FONT_SIZE_MAIN);
    paint.setColor(textShadowColor);
    canvas.drawText(percentStr, numPosX + TEXT_SHADOW_OFFSET,
      textPosY + TEXT_SHADOW_OFFSET, paint);
    paint.setColor(textColor);
    canvas.drawText(percentStr, numPosX, textPosY, paint);
    paint.setTextSize(FONT_SIZE_SIGN);
    paint.setColor(textShadowColor);
    canvas.drawText(percentSign, signPosX + TEXT_SHADOW_OFFSET,
      textPosY + TEXT_SHADOW_OFFSET, paint);
    paint.setColor(textColor);
    canvas.drawText(percentSign, signPosX, textPosY, paint);
    paint.setTextSize(FONT_SIZE_TIME);
    paint.setColor(textShadowColor);
    canvas.drawText(timeStr, timePosX + TEXT_SHADOW_OFFSET,
      timePosY + TEXT_SHADOW_OFFSET, paint);
    paint.setColor(textColor);
    canvas.drawText(timeStr, timePosX, timePosY, paint);
  }

  private String getTimeString(Resources res, long time) {
    if (time < 60) return res.getString(R.string.time_seconds, time);
    long seconds = time % 60; time /= 60;
    if (time < 60) return res.getString(R.string.time_minutes, time, seconds);
    long minutes = time % 60; time /= 60;
    if (time < 24) return res.getString(R.string.time_hours, time, minutes);
    long hours = time % 24; time /= 24;
    return res.getString(R.string.time_days, time, hours);
  }
}
