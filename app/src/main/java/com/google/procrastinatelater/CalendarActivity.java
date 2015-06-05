package com.google.procrastinatelater;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * http://droidwalk.blogspot.com/2012/11/android-calendar-sample.html
 * Created by Nicole on 16-May-15.
 */
public class CalendarActivity extends Activity {

    public Calendar month, itemMonth; //calendar instances
    public CalendarAdapter adapter; //adapter instance
    public Handler handler; //for grabbing some event values. used to show the dot marker
    public ArrayList<String> items; //container to store calendar items which need showing the event marker

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        month = Calendar.getInstance();
        itemMonth = (Calendar) month.clone();

        items = new ArrayList<String>();
        adapter = new CalendarAdapter(this, (GregorianCalendar)month);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(adapter);

        handler = new Handler();
        handler.post(calendarUpdater);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

        RelativeLayout previous = (RelativeLayout) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousMonth();
                refreshCalendar();
            }
        });

        RelativeLayout next = (RelativeLayout) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextMonth();
                refreshCalendar();
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                ((CalendarAdapter)parent.getAdapter()).setSelected(v);
                String selectedGridDate = CalendarAdapter.dayString.get(position);
                String[] separatedTime = selectedGridDate.split("-");
                String gridvalueString = separatedTime[2].replaceFirst("^0*", ""); //taking last part of date (like 2 from 2012-12-02)
                int gridValue = Integer.parseInt(gridvalueString);

                //navigate to next or previous month on clicking offdays
                if((gridValue > 10) && (position < 8)){
                    setPreviousMonth();
                    refreshCalendar();
                }else if((gridValue <7) && (position > 28)){
                    setNextMonth();
                    refreshCalendar();
                }
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);

                showToast(selectedGridDate);

            }
        });
    }

    protected void setNextMonth(){
        if (month.get(Calendar.MONTH) == month.getActualMaximum(Calendar.MONTH)){
            //new year
            month.set((month.get(Calendar.YEAR)+1), month.getActualMinimum(Calendar.MONTH), 1);
        }else{
            //move to next month, same year
            month.set(Calendar.MONTH, month.get(Calendar.MONTH)+1);
        }
    }

    protected void setPreviousMonth(){
        if (month.get(Calendar.MONTH) == month.getActualMinimum(Calendar.MONTH)){
            //one year back
            month.set((month.get(Calendar.YEAR)-1), month.getActualMaximum(Calendar.MONTH), 1);
        }else{
            //one month back, within the same year
            month.set(Calendar.MONTH, month.get(Calendar.MONTH)-1);
        }
    }

    protected void showToast(String string){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    public void refreshCalendar(){
        TextView title = (TextView) findViewById(R.id.title);

        adapter.refreshDays();
        adapter.notifyDataSetChanged();
        handler.post(calendarUpdater); //generate some calendar items

        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
    }

    public Runnable calendarUpdater = new Runnable() {
        @Override
        public void run() {
            items.clear();

            //Print dates of the current week
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String itemValue;
            //move through the week

            //He is adding events on dates HERE
            for (int i = 0; i < 7; i++){
                itemValue = df.format(itemMonth.getTime());
                itemMonth.add(Calendar.DATE, 1);
                items.add("2012-09-12");
                items.add("2012-10-07");
                items.add("2012-10-15");
                items.add("2012-10-20");
                items.add("2012-11-30");
                items.add("2012-11-28");
            }

            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };
}
