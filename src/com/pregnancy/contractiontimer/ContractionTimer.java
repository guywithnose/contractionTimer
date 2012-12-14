package com.pregnancy.contractiontimer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContractionTimer extends Activity
{
  SurfaceView timeGraph;
  ListView contractionData;
  Button startStopButton;
  TextView reminders;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contraction_timer);

    contractionData = (ListView) findViewById(R.id.contractionData);
    reminders = (TextView) findViewById(R.id.reminders);
    startStopButton = (Button) findViewById(R.id.startStopButton);
    timeGraph = (SurfaceView) findViewById(R.id.timeGraph);

    startStopButton.setOnClickListener(new OnClickListener()
    {
      @SuppressWarnings("unused")
      @Override
      public void onClick(View v)
      {
        if (startStopButton.getText().equals(getString(R.string.start)))
        {
          startStopButton.setText(getString(R.string.stop));
        } else
        {
          startStopButton.setText(getString(R.string.start));
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.activity_contraction_timer, menu);
    return true;
  }
}
