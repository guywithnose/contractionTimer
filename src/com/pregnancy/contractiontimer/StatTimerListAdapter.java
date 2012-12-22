/*
 * File: StatTimerListAdapter.java Author: Robert Bittle <guywithnose@gmail.com>
 */
package com.pregnancy.contractiontimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * The Class StatTimerListAdapter.
 */
public class StatTimerListAdapter extends BaseAdapter
{

  /** The timers. */
  Map<String, Long> timers;

  /** The timer order. */
  List<String> timerOrder;

  /** The change listener. */
  OnSharedPreferenceChangeListener changeListener;

  /** The context. */
  ContractionTimer context;

  /** The timer data. */
  private SharedPreferences timerData;

  StatData duration = new StatData("Duration");
  StatData startToStart = new StatData("Start To Start");
  StatData rest = new StatData("Rest");

  /**
   * Instantiates a new stat timer list adapter.
   * 
   * @param parent
   *          the parent
   * @param contractionData
   *          the contraction data
   */
  public StatTimerListAdapter(ContractionTimer parent,
      SharedPreferences contractionData)
  {
    context = parent;
    timerData = parent.getSharedPreferences("Timers", Context.MODE_PRIVATE);
    updateTimers();
    changeListener = new OnSharedPreferenceChangeListener()
    {

      @Override
      public void onSharedPreferenceChanged(
          SharedPreferences sharedPreferences,
          @SuppressWarnings("unused") String key)
      {
        updateStats(sharedPreferences);
      }
    };
    contractionData.registerOnSharedPreferenceChangeListener(changeListener);
    updateStats(contractionData);
    
    new Thread(new updateTimers()).start();
  }

  /**
   * Update timers.
   */
  @SuppressWarnings("unchecked")
  void updateTimers()
  {
    timers = (Map<String, Long>) timerData.getAll();
    timerOrder = new ArrayList<String>(timers.keySet());
    Collections.sort(timerOrder);
    notifyDataSetChanged();
  }

  /**
   * Update stats.
   * 
   * @param storedData
   *          the stored data
   */
  void updateStats(SharedPreferences storedData)
  {
    @SuppressWarnings("unchecked")
    Map<String, Long> allData = (Map<String, Long>) storedData.getAll();
    HashMap<Long, Long> contractionData = new HashMap<Long, Long>();
    for (String key : allData.keySet())
    {
      contractionData.put(Long.valueOf(key), allData.get(key));
    }
    List<Long> sortedData = new ArrayList<Long>(contractionData.keySet());
    Collections.sort(sortedData);
    Collections.reverse(sortedData);

    duration = new StatData("Duration");
    startToStart = new StatData("Start To Start");
    rest = new StatData("Rest");

    for (int i = 0; i < sortedData.size(); i++)
    {
      Long thisStart = sortedData.get(i);
      Long thisStop = contractionData.get(sortedData.get(i));
      if (i > 0)
      {
        Long nextStart = sortedData.get(i - 1);
        rest.add(nextStart - thisStop);
      }
      if (i < sortedData.size() - 1)
      {
        Long lastStart = sortedData.get(i + 1);
        startToStart.add(thisStart - lastStart);
      }
      duration.add(thisStop - thisStart);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getCount()
   */
  @Override
  public int getCount()
  {
    return timers.size() + 5;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getItem(int)
   */
  @Override
  public String getItem(int position)
  {
    if (position == 0)
    {
      return "Current";
    } else if (position == 1)
    {
      return "Duration";
    } else if (position == 2)
    {
      return "StartToStart";
    } else if (position == 3)
    {
      return "Rest";
    } else if (position == timerOrder.size() + 4) {
      return "New";
    }
    return timerOrder.get(position - 4);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getItemId(int)
   */
  @Override
  public long getItemId(int position)
  {
    return position;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getView(int, android.view.View,
   * android.view.ViewGroup)
   */
  @SuppressWarnings("unused")
  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    if (position == 0)
    {
      Long now = GregorianCalendar.getInstance().getTimeInMillis();
      String durationText = "";
      if (context.lastStart != 0)
      {
        durationText = ContractionTimer.formatDuration(now - context.lastStart);
      }

      return getSimpleView("Current Contraction: " + durationText);
    } else if (position == 1)
    {
      return getStatView(duration);
    } else if (position == 2)
    {
      return getStatView(startToStart);
    } else if (position == 3)
    {
      return getStatView(rest);
    } else if (position == timerOrder.size() + 4) {
      return getSimpleView("Add a new timer");
    }
    return getTimerView(position - 4);
  }

  private View getSimpleView(String text)
  {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View simpleView = inflater.inflate(android.R.layout.simple_list_item_1,
        null);

    TextView textView = (TextView) simpleView.findViewById(android.R.id.text1);

    textView.setText(text);

    return simpleView;
  }

  private View getStatView(StatData statData)
  {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View statView = inflater.inflate(R.layout.statlistitem, null);

    TextView title = (TextView) statView.findViewById(R.id.title);
    TextView ave5 = (TextView) statView.findViewById(R.id.ave5);
    TextView ave20 = (TextView) statView.findViewById(R.id.ave20);
    TextView ave50 = (TextView) statView.findViewById(R.id.ave50);
    TextView aveAll = (TextView) statView.findViewById(R.id.aveAll);

    title.setText(statData.title);
    ave5.setText(ContractionTimer.formatDuration(statData.getAverage(5)));
    ave20.setText(ContractionTimer.formatDuration(statData.getAverage(20)));
    ave50.setText(ContractionTimer.formatDuration(statData.getAverage(50)));
    aveAll.setText(ContractionTimer.formatDuration(statData.getAverage()));

    return statView;
  }

  private View getTimerView(int position)
  {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View timerView = inflater.inflate(R.layout.timerlistitem, null);

    TextView title = (TextView) timerView.findViewById(R.id.title);
    TextView durationText = (TextView) timerView.findViewById(R.id.duration);
    Button reset = (Button) timerView.findViewById(R.id.reset);
    Button delete = (Button) timerView.findViewById(R.id.delete);

    Long now = GregorianCalendar.getInstance().getTimeInMillis();

    title.setText(getItem(position));
    durationText.setText(ContractionTimer.formatDuration(now
        - timers.get(getItem(position))));

    return timerView;
  }

  /**
   * The Class currentUpdater.
   */
  private class updateTimers implements Runnable
  {
    /**
     * Instantiates a new reminder updater.
     */
    public updateTimers()
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
          wait(500);
          context.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              //Long now = GregorianCalendar.getInstance().getTimeInMillis();
              notifyDataSetChanged();
            }
          });
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * The Class statData.
   */
  private class StatData
  {
    /** The all data. */
    private List<Long> allData;
    public String title;

    public StatData(String name)
    {
      title = name;
      allData = new ArrayList<Long>();
    }

    /**
     * Adds the.
     * 
     * @param data
     *          the data
     */
    public void add(Long data)
    {
      allData.add(data);
    }

    /**
     * Get Average.
     * 
     * @return the average
     */
    public Long getAverage()
    {
      return getAverage(allData.size());
    }

    /**
     * Get Average.
     * 
     * @param numData
     *          the num data
     * @return the average
     */
    public Long getAverage(int numData)
    {
      if (numData > allData.size())
      {
        numData = allData.size();
      }

      if (numData == 0)
      {
        return 0L;
      }

      Long total = 0L;
      for (int i = 0; i < numData; i++)
      {
        total += allData.get(i);
      }

      return total / numData;
    }
  }
}
