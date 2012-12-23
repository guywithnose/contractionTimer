package com.pregnancy.contractiontimer;

import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AddTimerDialog extends DialogFragment
{
  TextView timerName;
  TextView timerAlertTime;
  
  @Override
  public Dialog onCreateDialog(@SuppressWarnings("unused") Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();

    View addTimerView = inflater.inflate(R.layout.createtimerdialog, null);
    timerName = (TextView)addTimerView.findViewById(R.id.timerName);
    timerAlertTime = (TextView)addTimerView.findViewById(R.id.timerAlertTime);
    builder.setView(addTimerView)
    // Add action buttons
        .setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
          @Override
          @SuppressWarnings("unused") 
          public void onClick(DialogInterface dialog, int id)
          {
            SharedPreferences timerData = getActivity().getSharedPreferences("Timers", Context.MODE_PRIVATE);
            Editor timerEditor = timerData.edit();
            Long now = GregorianCalendar.getInstance().getTimeInMillis();
            JSONObject timerInfo = new JSONObject();
            try
            {
              timerInfo.put("start", now);
              timerInfo.put("alertTime", Integer.valueOf(timerAlertTime.getText().toString()));
            } catch (JSONException e)
            {
              e.printStackTrace();
            }
            timerEditor.putString(timerName.getText().toString(), timerInfo.toString());
            timerEditor.commit();
          }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
          @SuppressWarnings("unused")
          @Override
          public void onClick(DialogInterface dialog, int id)
          {
            // Do Nothing
          }
        });
    return builder.create();
  }
}
