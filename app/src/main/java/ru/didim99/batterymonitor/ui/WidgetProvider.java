package ru.didim99.batterymonitor.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import ru.didim99.batterymonitor.R;
import ru.didim99.batterymonitor.utils.BatteryState;

/**
 * Created by didim99 on 06.07.19.
 */
public class WidgetProvider extends AppWidgetProvider {
  private static final int UPDATE_PERIOD = 60000;

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
    state.update(context);

    WidgetDrawer drawer = new WidgetDrawer(context);
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
    views.setImageViewBitmap(R.id.ivBackground, drawer.draw(state));
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
}
