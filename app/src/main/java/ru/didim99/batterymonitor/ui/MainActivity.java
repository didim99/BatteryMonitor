package ru.didim99.batterymonitor.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import androidx.appcompat.app.AppCompatActivity;
import ru.didim99.batterymonitor.BuildConfig;
import ru.didim99.batterymonitor.R;
import ru.didim99.batterymonitor.core.BatteryStat;
import ru.didim99.batterymonitor.core.BatteryState;
import ru.didim99.batterymonitor.core.LevelStat;
import ru.didim99.batterymonitor.utils.TimeUtils;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    setFinishOnTouchOutside(false);

    findViewById(R.id.btnOk).setOnClickListener(v -> finish());
    findViewById(R.id.btnRefresh).setOnClickListener(v ->
      WidgetProvider.requireUpdate(getApplicationContext()));

    TextView tvLifetimeTitle = findViewById(R.id.tvLifetimeTitle);
    TextView tvBatteryLevel = findViewById(R.id.tvBatteryLevel);
    TextView tvLifetime = findViewById(R.id.tvLifetime);
    TextView tvUptime = findViewById(R.id.tvUptime);
    TextView tvLastDCharge = findViewById(R.id.tvLastDCharge);
    TextView tvLastDUsage = findViewById(R.id.tvLastDUsage);

    Resources res = getResources();
    BatteryState state = BatteryState.load(this);
    if (state == null) return;

    BatteryStat stat = new BatteryStat(this, state);
    tvLifetimeTitle.setText(state.isCharging() ?
      R.string.stat_chargeTime : R.string.stat_lifeTime);
    tvBatteryLevel.setText(getString(
      R.string.percentStr, state.getPercent()));
    tvLifetime.setText(TimeUtils.getDetailedTimeString(
      res, stat.getLifeTime()));
    tvUptime.setText(describeTime(res, TimeUtils
      .millisToSeconds(SystemClock.elapsedRealtime())));
    tvLastDCharge.setText(describeStat(res, stat.getLastChgStat()));
    tvLastDUsage.setText(describeStat(res, stat.getLastUsgStat()));

    if (BuildConfig.DEBUG) {
      findViewById(R.id.btnRefresh).setOnLongClickListener(
        v -> takeWidgetPreview(stat));
    }
  }

  private String describeTime(Resources res, long time) {
    if (time < 0) return getString(R.string.no_data);
    else return TimeUtils.getDetailedTimeString(res, time);
  }

  private String describePercent(int percent) {
    if (percent < 0) return getString(R.string.no_data);
    else return getString(R.string.percentStr, percent);
  }

  private String describeStat(Resources res, LevelStat stat) {
    String time = describeTime(res, stat.getDurationSeconds());
    String startPct = describePercent(stat.getStart());
    String endPct = describePercent(stat.getEnd());
    return getString(R.string.time_percents, time, startPct, endPct);
  }

  private boolean takeWidgetPreview(BatteryStat stat) {
    File path = getExternalCacheDir();
    if (path == null) {
      Toast.makeText(this, getString(R.string.debug_unableToTakePreview,
        getString(R.string.debug_externalCacheNotFound)), Toast.LENGTH_LONG).show();
      return true;
    }

    String name = getResources().getResourceEntryName(R.drawable.widget_preview)
      .concat(".").concat(Bitmap.CompressFormat.PNG.name().toLowerCase());
    path = new File(path, name);

    try {
      WidgetDrawer drawer = new WidgetDrawer(this);
      drawer.drawToFile(stat, path.getAbsolutePath());
    } catch (IOException e) {
      Toast.makeText(this, getString(R.string.debug_unableToTakePreview,
        e.toString()), Toast.LENGTH_LONG).show();
    }

    Toast.makeText(this, getString(R.string.debug_previewTaken,
      path.getAbsolutePath()), Toast.LENGTH_LONG).show();
    return true;
  }
}
