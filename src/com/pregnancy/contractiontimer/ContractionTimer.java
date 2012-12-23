/*
 * File: ContractionTimer.java Author: Robert Bittle <guywithnose@gmail.com>
 */
package com.pregnancy.contractiontimer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class ContractionTimer extends Activity implements
    SurfaceHolder.Callback2
{
  private static final long REMINDERDELAY = 15000;

  SharedPreferences contractionData;
  SurfaceView timeGraph;
  ListView contractionList;
  ListView statsAndTimers;
  Button startStopButton;
  TextView reminders;
  public Long lastStart = 0L;
  ContractionListAdapter contractionAdapter;

  private StatTimerListAdapter statTimerAdapter;

  SurfaceHolder surfaceHolder;

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

    contractionAdapter = new ContractionListAdapter(this, contractionData);
    statTimerAdapter = new StatTimerListAdapter(this, contractionData);

    contractionList.setAdapter(contractionAdapter);
    statsAndTimers.setAdapter(statTimerAdapter);

    surfaceHolder = timeGraph.getHolder();
    surfaceHolder.addCallback(this);
    timeGraph.setWillNotDraw(false);

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
          contractionAdapter.notifyDataSetChanged();
        } else
        {
          startStopButton.setText(getString(R.string.start));
          contractionEditor.putLong(lastStart.toString(), now);
          lastStart = 0L;
          contractionEditor.commit();
          surfaceRedrawNeeded(surfaceHolder);
        }
      }
    });
  }

  void updateReminder()
  {
    String[] reminderText = getResources().getStringArray(R.array.reminders);
    int randomIndex = (int) Math.floor(Math.random() * reminderText.length);
    reminders.setText(Html.fromHtml(reminderText[randomIndex]),
        BufferType.SPANNABLE);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.activity_contraction_timer, menu);
    menu.findItem(R.id.menu_clear).setOnMenuItemClickListener(
        new OnMenuItemClickListener()
        {
          @Override
          public boolean onMenuItemClick(
              @SuppressWarnings("unused") MenuItem item)
          {
            Editor contractionEditor = contractionData.edit();
            for (String key : contractionData.getAll().keySet())
            {
              contractionEditor.remove(key);
            }
            contractionEditor.commit();
            surfaceRedrawNeeded(surfaceHolder);
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

  @Override
  protected void onPause()
  {
    super.onPause();
    statTimerAdapter.pause();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    statTimerAdapter.resume();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
   * , int, int, int)
   */
  @SuppressWarnings("unused")
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height)
  {
    tryDrawing(holder);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
   * )
   */
  @Override
  public void surfaceCreated(SurfaceHolder holder)
  {
    tryDrawing(holder);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder
   * )
   */
  @Override
  public void surfaceDestroyed(SurfaceHolder holder)
  {
    // Do Nothing
  }

  private void tryDrawing(SurfaceHolder holder)
  {
    Canvas canvas = holder.lockCanvas();
    if (canvas == null)
    {
    } else
    {
      drawMyStuff(canvas);
      holder.unlockCanvasAndPost(canvas);
    }
  }

  private void drawMyStuff(final Canvas canvas)
  {
    canvas.drawRGB(255, 255, 255);

    Paint paint = new Paint();
    //paint.setDither(true);
    paint.setColor(0xFF000000);
    paint.setStyle(Paint.Style.STROKE);
    //paint.setStrokeJoin(Paint.Join.ROUND);
    //paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth(3);

    Map<String, Long> allData = (Map<String, Long>) contractionData.getAll();
    if (allData.size() == 0)
    {
      return;
    }
    Map<Long, Long> data = new HashMap<Long, Long>();
    for (String key : allData.keySet())
    {
      data.put(Long.valueOf(key), allData.get(key));
    }
    List<Long> sortedData = new ArrayList<Long>(data.keySet());
    Collections.sort(sortedData);
    Collections.reverse(sortedData);

    if (sortedData.size() > 20)
    {
      sortedData = sortedData.subList(0, 20);
    }

    Long now = GregorianCalendar.getInstance().getTimeInMillis();

    long msPerPixel = (now - sortedData.get(sortedData.size() - 1))
        / canvas.getWidth();

    for (int i = 0; i < sortedData.size(); i++)
    {
      Long start = sortedData.get(i);
      Long stop = data.get(start);
      float startX = canvas.getWidth() - ((now - start) / msPerPixel);
      float stopX = canvas.getWidth() - ((now - stop) / msPerPixel);
      float onY = canvas.getHeight() * 1 / 3;
      float offY = canvas.getHeight() * 2 / 3;
      //TODO fix the arcs
      //canvas.drawLine(startX, onY, stopX, onY, paint);
      //canvas.drawLine(stopX, onY, stopX, offY, paint);
      canvas.drawArc(new RectF(startX, onY, stopX, canvas.getHeight()), 180, 180, false, paint);
      if (i < sortedData.size() - 1)
      {
        Long laststart = sortedData.get(i + 1);
        Long laststop = data.get(laststart);
        startX = canvas.getWidth() - ((now - laststop) / msPerPixel);
        stopX = canvas.getWidth() - ((now - start) / msPerPixel);
        canvas.drawLine(startX, offY, stopX, offY, paint);
        //canvas.drawLine(stopX, offY, stopX, onY, paint);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.view.SurfaceHolder.Callback2#surfaceRedrawNeeded(android.view.
   * SurfaceHolder)
   */
  @Override
  public void surfaceRedrawNeeded(SurfaceHolder holder)
  {
    tryDrawing(holder);
  }
}
