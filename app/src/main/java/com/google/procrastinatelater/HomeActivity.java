package com.google.procrastinatelater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;


public class HomeActivity extends Activity {
    LinearLayout toProjectsLayout, toCalendarLayout;
    LinearLayout todoLayout;
    TextView projectsMessage; //message is removed from todoLayout, and replaces the layout in todoFrame if the layout is empty

    List<Project> Projects = new ArrayList<>();
    DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //these buttons' onClicks are set in activity_home.xml
        toProjectsLayout = (LinearLayout) findViewById(R.id.toProjectsLayout);
        toCalendarLayout = (LinearLayout) findViewById(R.id.toCalendarLayout);
        todoLayout = (LinearLayout) findViewById(R.id.todoLayout);
        projectsMessage = (TextView) findViewById(R.id.projectsMessage); //message is removed from todoLayout, and replaces the layout in todoFrame if the layout is empty

    }

    //update project number every time we return to the home activity
    @Override
    protected void onResume(){
        super.onResume();
        //connect to database. get id's for project events
        todoLayout.removeAllViews();
        dbHandler = new DatabaseHandler(getApplicationContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat("KK:mm a");
        Date startDate;
        Date endDate;
        //sql here.

        //event_id vs _id???
        String[] projection = new String[] { CalendarContract.Events.ORIGINAL_ID,
                CalendarContract.Events.TITLE, CalendarContract.Events.ALL_DAY,
                CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND};
        // 0 = January, 1 = February, ...
        Calendar startToday = Calendar.getInstance();
        startToday.set(Calendar.HOUR_OF_DAY, 0);
        startToday.set(Calendar.MINUTE, 0);
        startToday.set(Calendar.SECOND, 0);
        Calendar endToday= Calendar.getInstance();
        endToday.set(Calendar.HOUR_OF_DAY, 23);
        endToday.set(Calendar.MINUTE, 59);
        endToday.set(Calendar.SECOND, 59);

        // the range is all data from today to tomorrow
        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startToday.getTimeInMillis() + " ) AND ( "
                + CalendarContract.Events.DTSTART + " <= " + endToday.getTimeInMillis() + " ))";


        Cursor cursor = this.getBaseContext().getContentResolver().query( CalendarContract.Events.CONTENT_URI, projection, selection, null, null );

        // output the events
        if (cursor.moveToFirst()) {
            do {
                //check event ID.
                //Toast.makeText( this.getApplicationContext(), "Event " + cursor.getString(1) + " from Calendar " + cursor.getInt(0)+ " starting at " + (new Date(cursor.getLong(3))).toString(), Toast.LENGTH_LONG ).show();

                View event = getLayoutInflater().inflate(R.layout.todo_item, null);
                TextView todoTitle = (TextView)event.findViewById(R.id.todoTitle);
                TextView todoTime = (TextView)event.findViewById(R.id.todoTime);
                TextView todoLength = (TextView)event.findViewById(R.id.todoLength);

                //set text field values
                todoTitle.setText(cursor.getString(1)); //event title
                    startDate = new Date(cursor.getLong(3));
                    endDate = new Date(cursor.getLong(4));
                    long duration = endDate.getTime()-startDate.getTime();
                    int hrs = (int)(duration/(1000*60*60));
                    int mins = (int) (duration - (1000*60*60*hrs)) / (1000*60);
                    String text = "";
                if (hrs > 0){
                    text += (hrs + " hrs ");
                }
                if (mins > 0){
                    text += (mins + " mins");
                }
                todoLength.setText(text); //duration
                todoTime.setText(dateFormat.format(startDate)); //starting time

                todoLayout.addView(event);

            } while ( cursor.moveToNext());
        }else{      //there are no sessions today
            todoLayout.addView(projectsMessage);
        }
        cursor.close();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showHelp(View view){
        //go to help screen
        Intent getHelpScreenIntent = new Intent(this, HelpActivity.class);
        startActivity(getHelpScreenIntent);
        //setContentView(R.layout.help_layout);
    }

    public void goToProjects(View view) {
        //go to projects 'page'
        Intent getProjectsScreenIntent = new Intent(this, ProjectsList.class);
        startActivity(getProjectsScreenIntent);

    }

    public void goToCalendar(View view) {
        //to user's calendar
        try {
            ComponentName cn;
            Intent i = new Intent();
            cn = new ComponentName("com.android.calendar", "com.android.calendar.LaunchActivity");
            i.setComponent(cn);
            startActivity(i);
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(), "Sorry, this feature is not compatible with your Android version.", Toast.LENGTH_LONG).show();
        }
    }

    private void findEvents(){

    }

}
