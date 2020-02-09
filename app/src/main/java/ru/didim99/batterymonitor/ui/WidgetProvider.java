package ru.didim99.batterymonitor.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.widget.RemoteViews;
import ru.didim99.batterymonitor.R;
import ru.didim99.batterymonitor.utils.ColorScale;

/**
 * Created by didim99 on 06.07.19.
 */
public class WidgetProvider extends AppWidgetProvider {
  private static final String FONT_PATH = "fonts/LCDNova.ttf";
  private static final int UPDATE_PERIOD = 60000;
  private static final int BATTERY_LOW_LEVEL = 15;
  private static final double BATTERY_ZERO_LEVEL = 0.0;
  private static final double BATTERY_HALF_LEVEL = 0.5;
  private static final double BATTERY_FULL_LEVEL = 1.0;
  private static final int DEFAULT_VALUE = -1;
  private static final int CANVAS_WIDTH = 400;
  private static final int CANVAS_HEIGHT = 800;
  private static final int FONT_SIZE_MAIN = 150;
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
      Intent updateIntent = new Intent();
      updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
      context.sendBroadcast(updateIntent);
    }
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent battery = context.registerReceiver(null, filter);
    if (battery == null) return;

    int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, DEFAULT_VALUE);
    int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, DEFAULT_VALUE);
    int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_VALUE);
    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
      status == BatteryManager.BATTERY_STATUS_FULL;
    int percent = Math.round(level / (float) scale * 100f);
    boolean isLow = percent <= BATTERY_LOW_LEVEL;

    int textColor;
    Resources res = context.getResources();
    if (isCharging) textColor = res.getColor(R.color.widgetTextCharging);
    else if (isLow) textColor = res.getColor(R.color.widgetTextLow);
    else textColor = res.getColor(R.color.widgetTextNormal);
    int textShadowColor = res.getColor(R.color.widgetTextShadow);
    int bgColor = res.getColor(R.color.widgetBackground);
    ColorScale colorScale = new ColorScale(
      new ColorScale.Point(BATTERY_ZERO_LEVEL, res.getColor(R.color.batteryZero)),
      new ColorScale.Point(BATTERY_HALF_LEVEL, res.getColor(R.color.batteryHalf)),
      new ColorScale.Point(BATTERY_FULL_LEVEL, res.getColor(R.color.batteryFull)));
    String percentSign = res.getString(R.string.percentSign);
    String percentStr = String.valueOf(percent);

    Typeface tf = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
    Bitmap bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setTypeface(tf);
    paint.setSubpixelText(true);
    paint.setAntiAlias(true);

    int numWidth = FONT_SIZE_MAIN / 2 * percentStr.length();
    int textWidth = numWidth + SIGN_MARGIN + FONT_SIZE_SIGN;
    int numPosX = (CANVAS_WIDTH - textWidth) / 2;
    int signPosX = numPosX + numWidth + SIGN_MARGIN;
    int textPosY = (CANVAS_HEIGHT + FONT_SIZE_MAIN) / 2;

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

    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
    views.setImageViewBitmap(R.id.ivBackground, bitmap);
    manager.updateAppWidget(appWidgetIds, views);

    Intent updateIntent = new Intent();
    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC, System.currentTimeMillis() + UPDATE_PERIOD,
      PendingIntent.getBroadcast(context, 0, updateIntent, 0));
  }
}
