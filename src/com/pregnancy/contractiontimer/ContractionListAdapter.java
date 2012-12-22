/*
 * File: ContractionListAdapter.java Author: Robert Bittle
 * <guywithnose@gmail.com>
 */
package com.pregnancy.contractiontimer;

import java.util.ArrayList;
import java.util.Collections;
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
import android.widget.TextView;

/**
 * @author Dude
 * 
 */
public class ContractionListAdapter extends BaseAdapter
{
  Map<Long, Long> contractionData;
  List<Long> sortedData;
  ContractionTimer context;
  OnSharedPreferenceChangeListener changeListener;

  public ContractionListAdapter(ContractionTimer parentContext,
      SharedPreferences storedData)
  {
    context = parentContext;
    updateData(storedData);
    changeListener = new OnSharedPreferenceChangeListener()
    {
      @Override
      public void onSharedPreferenceChanged(
          SharedPreferences sharedPreferences,
          @SuppressWarnings("unused") String key)
      {
        updateData(sharedPreferences);
      }
    };
    storedData.registerOnSharedPreferenceChangeListener(changeListener);
  }

  /**
   * Update data.
   * 
   * @param storedData
   *          the stored data
   */
  void updateData(SharedPreferences storedData)
  {
    @SuppressWarnings("unchecked")
    Map<String, Long> allData = (Map<String, Long>) storedData.getAll();
    contractionData = new HashMap<Long, Long>();
    for (String key : allData.keySet())
    {
      contractionData.put(Long.valueOf(key), allData.get(key));
    }
    sortedData = new ArrayList<Long>(contractionData.keySet());
    Collections.sort(sortedData);
    Collections.reverse(sortedData);
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
    return sortedData.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.Adapter#getItem(int)
   */
  @Override
  public Long getItem(int position)
  {
    return sortedData.get(position);
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
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View contractionView = inflater.inflate(R.layout.contractionlistitem, null);

    TextView start = (TextView) contractionView.findViewById(R.id.start);
    TextView stop = (TextView) contractionView.findViewById(R.id.stop);

    TextView duration = (TextView) contractionView.findViewById(R.id.duration);
    TextView startToStart = (TextView) contractionView
        .findViewById(R.id.startToStart);
    TextView restPeriod = (TextView) contractionView.findViewById(R.id.rest);

    start.setText(ContractionTimer.formatTime(getItem(position)));
    stop.setText(ContractionTimer.formatTime(contractionData.get(getItem(position))));

    Long thisStart = getItem(position);
    Long thisStop = contractionData.get(getItem(position));
    if (position == 0)
    {
      if (context.lastStart != 0L)
      {
        restPeriod.setText(ContractionTimer.formatDuration(context.lastStart - thisStop));
      } else
      {
        restPeriod.setText(context.getString(R.string.NA));
      }
    } else if (position == sortedData.size() - 1)
    {
      startToStart.setText(context.getString(R.string.NA));
    }

    if (position > 0)
    {
      Long nextStart = getItem(position - 1);

      restPeriod.setText(ContractionTimer.formatDuration(nextStart - thisStop));
    }
    if (position < sortedData.size() - 1)
    {
      Long lastStart = getItem(position + 1);

      startToStart.setText(ContractionTimer.formatDuration(thisStart - lastStart));
    }
    duration.setText(ContractionTimer.formatDuration(thisStop - thisStart));

    return contractionView;
  }
}