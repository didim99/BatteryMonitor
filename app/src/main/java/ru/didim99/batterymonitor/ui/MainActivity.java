package ru.didim99.batterymonitor.ui;

import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import ru.didim99.batterymonitor.R;
import ru.didim99.batterymonitor.utils.BatteryState;
import ru.didim99.batterymonitor.utils.TimeUtils;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    setFinishOnTouchOutside(false);

    findViewById(R.id.btnOk).setOnClickListener(v -> finish());
    TextView tvLifetimeTitle = findViewById(R.id.tvLifetimeTitle);
    TextView tvBatteryLevel = findViewById(R.id.tvBatteryLevel);
    TextView tvLifetime = findViewById(R.id.tvLifetime);
    TextView tvUptime = findViewById(R.id.tvUptime);
    TextView tvLastDCharge = findViewById(R.id.tvLastDCharge);
    TextView tvLastDUsage = findViewById(R.id.tvLastDUsage);

    Resources res = getResources();
    BatteryState state = BatteryState.load(this);
    tvLifetimeTitle.setText(state.isCharging() ?
      R.string.stat_chargeTime : R.string.stat_lifeTime);
    tvBatteryLevel.setText(getString(
      R.string.percentStr, state.getPercent()));
    tvLifetime.setText(describeTime(res, state.getLifeTime()));
    tvUptime.setText(describeTime(res, SystemClock.elapsedRealtime() / 1000));
    tvLastDCharge.setText(describeTime(res, state.getLastDCharge()));
    tvLastDUsage.setText(describeTime(res, state.getLastDUsage()));
  }

  private String describeTime(Resources res, long time) {
    if (time == 0) return getString(R.string.no_data);
    else return TimeUtils.getDetailedTimeString(res, time);
  }
}
