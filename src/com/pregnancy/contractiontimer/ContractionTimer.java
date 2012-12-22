/*
 * File: ContractionTimer.java Author: Robert Bittle <guywithnose@gmail.com>
 */
package com.pregnancy.contractiontimer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContractionTimer extends Activity
{
  private static final long REMINDERDELAY = 15000;

  SharedPreferences contractionData;
  SurfaceView timeGraph;
  ListView contractionList;
  ListView statsAndTimers;
  Button startStopButton;
  TextView reminders;
  public Long lastStart = 0L;
  BaseAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contraction_timer);

    contractionList = (ListView) findViewById(R.id.contractionList);
    statsAndTimers = (ListView) findViewById(R.id.statsAndTimers);
    reminders = (TextView) findViewById(R.id.reminders);
    startStopButton = (Button) findViewById(R.id.startStopButton);
    timeGraph = (SurfaceView) findViewById(R.id.timeGraph);
    contractionData = getSharedPreferences("Contractions", MODE_PRIVATE);

    adapter = new ContractionListAdapter(this, contractionData);

    contractionList.setAdapter(adapter);
    statsAndTimers.setAdapter(new StatTimerListAdapter(this, contractionData));

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
          adapter.notifyDataSetChanged();
        } else
        {
          startStopButton.setText(getString(R.string.start));
          contractionEditor.putLong(lastStart.toString(), now);
          lastStart = 0L;
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
    menu.findItem(R.id.menu_clear).setOnMenuItemClickListener(new OnMenuItemClickListener()
    {
      @Override
      public boolean onMenuItemClick(@SuppressWarnings("unused") MenuItem item)
      {
        Editor contractionEditor = contractionData.edit();
        for (String key : contractionData.getAll().keySet())
        {
          contractionEditor.remove(key);
        }
        contractionEditor.commit();
        return false;
      }
    });
    return true;
  }

  /**
   * Format time.
   * 
   * @param time
   *          the time
   * @return the string
   */
  public static String formatDuration(Long time)
  {
    Long seconds = time / 1000;
    Long hours = seconds / 3600;
    Long minutes = (seconds % 3600) / 60;
    seconds = seconds % 60;
    String duration = "";
    if (hours > 0)
    {
      duration += hours + "h ";
    }
    if (minutes > 0)
    {
      duration += minutes + "m ";
    }
    if (seconds > 0)
    {
      duration += seconds + "s";
    }
    return duration;
  }

  /**
   * Format time.
   * 
   * @param time
   *          the time
   * @return the string
   */
  public static String formatTime(Long time)
  {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeInMillis(time);
    return cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":"
        + cal.get(Calendar.SECOND);
  }

  /**
   * The Class reminderUpdater.
   */
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
