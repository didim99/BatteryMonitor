package ru.didim99.batterymonitor.ui;

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

    BatteryState state = BatteryState.load(this);
    tvLifetimeTitle.setText(state.isCharging() ?
      R.string.stat_chargeTime : R.string.stat_lifeTime);
    tvBatteryLevel.setText(getString(
      R.string.percentStr, state.getPercent()));
    tvLifetime.setText(TimeUtils.getDetailedTimeString(
      getResources(), state.getLifeTime()));
    tvUptime.setText(TimeUtils.getDetailedTimeString(
      getResources(), SystemClock.elapsedRealtime() / 1000));
  }
}
