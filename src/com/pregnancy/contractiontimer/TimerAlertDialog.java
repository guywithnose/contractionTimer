package com.pregnancy.contractiontimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class TimerAlertDialog extends DialogFragment
{
  String timerName = "";

  @Override
  public Dialog onCreateDialog(
      @SuppressWarnings("unused") Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(timerName).setPositiveButton("OK", null);
    return builder.create();
  }

  /**
   * 
   */
  public void initialize()
  {
    timerName = getArguments().getString("timerName");
  }
}
