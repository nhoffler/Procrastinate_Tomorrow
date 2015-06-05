package com.google.procrastinatelater;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Nicole on 16-May-15.
 */
public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    EditText txtDue;

    public DateDialog(View view){
        txtDue = (EditText)view;
    }

    public Dialog onCreateDialog(Bundle savedInstance){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day){

        String date = getMonth(month) + " "  + day + ", " + year;
        txtDue.setText(date);
    }

    public String getMonth(int month){
        return new DateFormatSymbols().getMonths()[month];
    }

}
