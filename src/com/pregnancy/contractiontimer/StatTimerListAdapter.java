/*
 * File: StatTimerListAdapter.java Author: Robert Bittle <guywithnose@gmail.com>
 */
package com.pregnancy.contractiontimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * The Class StatTimerListAdapter.
 */
public class StatTimerListAdapter implements ListAdapter
{

  /** The Constant MILISECONDSINAMINUTE. */
  public static final Long MILISECONDSINAMINUTE = 60000L;

  /** The Constant SNOOZE. */
  private static final Long SNOOZE = 300000L;

  /** The timers. */
  Map<String, String> timers;

  /** The timer order. */
  List<String> timerOrder;

  /** The change listener. */
  OnSharedPreferenceChangeListener contractionListener;

  /** The change listener. */
  OnSharedPreferenceChangeListener timerListener;

  /** The context. */
  ContractionTimer context;

  /** The timer data. */
  SharedPreferences timerData;

  /** The reset click. */
  OnClickListener resetClick;

  /** The delete click. */
  OnClickListener deleteClick;

  /** The duration. */
  StatData duration = new StatData("Duration");

  /** The start to start. */
  StatData startToStart = new StatData("Start To Start");

  /** The rest. */
  StatData rest = new StatData("Rest");

  /** The observers. */
  List<DataSetObserver> observers = new LinkedList<DataSetObserver>();

  /** The new timer click. */
  private OnClickListener newTimerClick;

  boolean paused = false;

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
    contractionListener = new OnSharedPreferenceChangeListener()
    {
      @Override
      public void onSharedPreferenceChanged(
          SharedPreferences sharedPreferences,
          @SuppressWarnings("unused") String key)
      {
        updateStats(sharedPreferences);
      }
    };
    timerListener = new OnSharedPreferenceChangeListener()
    {
      @Override
      @SuppressWarnings("unused")
      public void onSharedPreferenceChanged(
          SharedPreferences sharedPreferences, String key)
      {
        updateTimers();
      }
    };
    contractionData
        .registerOnSharedPreferenceChangeListener(contractionListener);
    timerData.registerOnSharedPreferenceChangeListener(timerListener);
    updateStats(contractionData);

    new Thread(new UpdateTimers()).start();

    deleteClick = new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        View parentView = (View) v.getParent();
        TextView titleText = (TextView) parentView.findViewById(R.id.title);
        Editor timerEditor = timerData.edit();
        timerEditor.remove(titleText.getText().toString());
        timerEditor.commit();
      }
    };

    resetClick = new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        View parentView = (View) v.getParent();
        TextView titleText = (TextView) parentView.findViewById(R.id.title);
        Editor timerEditor = timerData.edit();
        Long now = GregorianCalendar.getInstance().getTimeInMillis();
        try
        {
          JSONObject timerInfo = new JSONObject(timerData.getString(titleText
              .getText().toString(), "{}"));
          timerInfo.put("start", now);
          timerEditor.putString(titleText.getText().toString(),
              timerInfo.toString());
          timerEditor.commit();
        } catch (JSONException e)
        {
          e.printStackTrace();
        }
      }
    };

    newTimerClick = new OnClickListener()
    {
      @SuppressWarnings("unused")
      @Override
      public void onClick(View v)
      {
        AddTimerDialog newFragment = new AddTimerDialog();
        newFragment.show(context.getFragmentManager(), "Add Timer");
      }
    };
  }

  /**
   * Update timers.
   */
  @SuppressWarnings("unchecked")
  void updateTimers()
  {
    timers = (Map<String, String>) timerData.getAll();
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
    notifyDataSetChanged();
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
    } else if (position == timerOrder.size() + 4)
    {
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
      View currentContractionView = getSimpleView(getCurrentDurationText(),
          convertView);
      currentContractionView.setOnClickListener(null);
      return currentContractionView;
    } else if (position == 1)
    {
      return getStatView(duration, convertView);
    } else if (position == 2)
    {
      return getStatView(startToStart, convertView);
    } else if (position == 3)
    {
      return getStatView(rest, convertView);
    } else if (position == timerOrder.size() + 4)
    {
      View newTimerView = getSimpleView("Add a new timer", convertView);
      newTimerView.setOnClickListener(newTimerClick);
      newTimerView.setMinimumHeight(100);
      return newTimerView;
    }
    return getTimerView(position - 4, convertView);
  }

  /**
   * Get Current duration text.
   * 
   * @return the current duration text
   */
  String getCurrentDurationText()
  {
    Long now = GregorianCalendar.getInstance().getTimeInMillis();
    String durationText = "";
    if (context.lastStart != 0)
    {
      durationText = ContractionTimer.formatDuration(now - context.lastStart);
    }

    return "Current Contraction: " + durationText;
  }

  /**
   * Get Simple view.
   * 
   * @param text
   *          the text
   * @param simpleView
   *          the simple view
   * @return the simple view
   */
  private View getSimpleView(String text, View simpleView)
  {
    if (simpleView == null)
    {
      LayoutInflater inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      simpleView = inflater.inflate(android.R.layout.simple_list_item_1, null);
    }

    TextView textView = (TextView) simpleView.findViewById(android.R.id.text1);

    textView.setText(text);

    return simpleView;
  }

  /**
   * Get Stat view.
   * 
   * @param statData
   *          the stat data
   * @param statView
   *          the stat view
   * @return the stat view
   */
  private View getStatView(StatData statData, View statView)
  {
    if (statView == null)
    {
      LayoutInflater inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      statView = inflater.inflate(R.layout.statlistitem, null);
    }

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

  /**
   * Get Timer view.
   * 
   * @param position
   *          the position
   * @param timerView
   *          the timer view
   * @return the timer view
   */
  private View getTimerView(int position, View timerView)
  {
    if (timerView == null)
    {
      LayoutInflater inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      timerView = inflater.inflate(R.layout.timerlistitem, null);
      Button reset = (Button) timerView.findViewById(R.id.reset);
      Button delete = (Button) timerView.findViewById(R.id.delete);
      delete.setOnClickListener(deleteClick);
      reset.setOnClickListener(resetClick);
    }

    TextView title = (TextView) timerView.findViewById(R.id.title);
    TextView durationText = (TextView) timerView.findViewById(R.id.duration);
    TextView timeLeft = (TextView) timerView.findViewById(R.id.timeLeft);

    Long now = GregorianCalendar.getInstance().getTimeInMillis();

    title.setText(timerOrder.get(position));
    try
    {
      JSONObject timerInfo = new JSONObject(
          timers.get(timerOrder.get(position)));
      Long timerStart = timerInfo.getLong("start");
      Long alertDuration = MILISECONDSINAMINUTE * timerInfo.getInt("alertTime");
      durationText.setText(ContractionTimer.formatDuration(now - timerStart));
      timeLeft.setText(ContractionTimer.formatDuration(timerStart + alertDuration - now));
    } catch (JSONException e)
    {
      timerData.edit().remove(timerOrder.get(position)).commit();
      e.printStackTrace();
    }

    return timerView;
  }

  /**
   * The Class currentUpdater.
   */
  private class UpdateTimers implements Runnable
  {
    /**
     * Instantiates a new reminder updater.
     */
    public UpdateTimers()
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
        if (!paused)
        {
          try
          {
            wait(500);
            checkTimers();
            context.runOnUiThread(new Runnable()
            {
              @Override
              public void run()
              {
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
  }

  /**
   * Notify data set changed.
   */
  public void notifyDataSetChanged()
  {
    for (DataSetObserver observer : observers)
    {
      observer.onChanged();
    }
  }

  /**
   * Check timers.
   */
  public void checkTimers()
  {
    for (String key : timers.keySet())
    {
      try
      {
        JSONObject timerInfo = new JSONObject(timers.get(key));
        Long start = timerInfo.getLong("start");
        Integer alertTime = timerInfo.getInt("alertTime");
        Long now = GregorianCalendar.getInstance().getTimeInMillis();
        Long alertDuration = (MILISECONDSINAMINUTE * alertTime);
        if (now - start >= alertDuration)
        {
          Long newStart = start
              + Math.min(alertDuration, SNOOZE);
          // If we've been away a while we don't need a bunch of notices.
          if (newStart + alertDuration < now)
          {
            newStart = now;
            // Next snooze should be no more then 5 min away.
            if (alertTime > 5)
            {
              newStart = now - MILISECONDSINAMINUTE * 5;
            }
          }
          timerInfo.put("start", newStart);
          Editor timerEditor = timerData.edit();
          timerEditor.putString(key, timerInfo.toString());
          timerEditor.commit();
          context.runOnUiThread(new showTimerDialog(key));
        }
      } catch (JSONException e)
      {
        e.printStackTrace();
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

    /** The title. */
    public String title;

    /**
     * Instantiates a new stat data.
     * 
     * @param name
     *          the name
     */
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

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getItemViewType(int)
   */
  @Override
  public int getItemViewType(int position)
  {
    if (position == 0)
    {
      return 0;
    } else if (position == 1)
    {
      return 1;
    } else if (position == 2)
    {
      return 1;
    } else if (position == 3)
    {
      return 1;
    } else if (position == timerOrder.size() + 4)
    {
      return 0;
    }
    return 2;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getViewTypeCount()
   */
  @Override
  public int getViewTypeCount()
  {
    return 3;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#hasStableIds()
   */
  @Override
  public boolean hasStableIds()
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#isEmpty()
   */
  @Override
  public boolean isEmpty()
  {
    return getCount() == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver
   * )
   */
  @Override
  public void registerDataSetObserver(DataSetObserver observer)
  {
    if (!observers.contains(observer))
    {
      observers.add(observer);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#unregisterDataSetObserver(android.database.
   * DataSetObserver)
   */
  @Override
  public void unregisterDataSetObserver(DataSetObserver observer)
  {
    observers.remove(observer);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.ListAdapter#areAllItemsEnabled()
   */
  @Override
  public boolean areAllItemsEnabled()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.ListAdapter#isEnabled(int)
   */
  @Override
  public boolean isEnabled(@SuppressWarnings("unused") int position)
  {
    return true;
  }

  /**
   * The Class showTimerDialog.
   */
  private class showTimerDialog implements Runnable
  {
    String timerName;

    public showTimerDialog(String theTimerName)
    {
      timerName = theTimerName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      Bundle timerArgs = new Bundle();
      timerArgs.putString("timerName", timerName);
      TimerAlertDialog timerAlertDialog = new TimerAlertDialog();
      timerAlertDialog.setArguments(timerArgs);
      timerAlertDialog.initialize();
      timerAlertDialog.show(context.getFragmentManager(), timerName);
    }
  }

  public void pause()
  {
    paused = true;
  }

  public void resume()
  {
    paused = false;
  }
}
