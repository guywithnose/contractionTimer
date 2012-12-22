/*
 * File:         ContractionTimer.java
 * Author:       Robert Bittle <guywithnose@gmail.com>
 */
package com.pregnancy.contractiontimer;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContractionTimer extends Activity
{
  private static final long REMINDERDELAY = 5000;

  SharedPreferences contractionData;
  SurfaceView timeGraph;
  ListView contractionList;
  Button startStopButton;
  TextView reminders;
  Map<Long, Long> contractionTimes = new HashMap<Long, Long>();
  Long lastStart;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contraction_timer);

    contractionList = (ListView) findViewById(R.id.contractionList);
    reminders = (TextView) findViewById(R.id.reminders);
    startStopButton = (Button) findViewById(R.id.startStopButton);
    timeGraph = (SurfaceView) findViewById(R.id.timeGraph);
    contractionData = getSharedPreferences("Contractions", MODE_PRIVATE);

    updateReminder();
    new Thread(new reminderUpdater()).start();

    startStopButton.setOnClickListener(new OnClickListener()
    {
      @SuppressWarnings("unused")
      @Override
      public void onClick(View v)
      {
        Editor contractionEditor = contractionData.edit();
        Long now = GregorianCalendar.getInstance().getTimeInMillis();
        if (startStopButton.getText().equals(getString(R.string.start)))
        {
          startStopButton.setText(getString(R.string.stop));
          lastStart = now;
        } else
        {
          startStopButton.setText(getString(R.string.start));
          contractionTimes.put(lastStart, now);
          contractionEditor.putLong(lastStart.toString(), now);
          contractionEditor.commit();
        }
      }
    });
  }

  void updateReminder()
  {
    String[] reminderText = getResources().getStringArray(R.array.reminders);
    int randomIndex = (int) Math.floor(Math.random() * reminderText.length);
    reminders.setText(reminderText[randomIndex]);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.activity_contraction_timer, menu);
    return true;
  }

  private class reminderUpdater implements Runnable
  {
    /**
     * Instantiates a new reminder updater.
     */
    public reminderUpdater()
    {
      // Default constructor
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public synchronized void run()
    {
      while (true)
      {
        try
        {
          wait(REMINDERDELAY);
          runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              updateReminder();
            }
          });
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
}
