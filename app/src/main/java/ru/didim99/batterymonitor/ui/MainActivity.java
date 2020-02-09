package ru.didim99.batterymonitor.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import ru.didim99.batterymonitor.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    setFinishOnTouchOutside(false);

    findViewById(R.id.btnOk).setOnClickListener(v -> finish());
  }
}
