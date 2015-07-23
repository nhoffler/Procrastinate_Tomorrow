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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class HomeActivity extends Activity {
    LinearLayout toProjectsLayout, toCalendarLayout;
    TextView projectsMessage;

    List<Project> Projects = new ArrayList<>();
    DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //these buttons' onClicks are set in activity_home.xml
        toProjectsLayout = (LinearLayout) findViewById(R.id.toProjectsLayout);
        toCalendarLayout = (LinearLayout) findViewById(R.id.toCalendarLayout);
        projectsMessage = (TextView) findViewById(R.id.projectsMessage);
    }

    //update project number every time we return to the home activity
    @Override
    protected void onResume(){
        super.onResume();
        //connect to database. get id's for project events
        LinearLayout todoLayout = (LinearLayout) findViewById(R.id.todoLayout);
        dbHandler = new DatabaseHandler(getApplicationContext());
        //sql here.

        String[] projection = new String[] { CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION };
        // 0 = January, 1 = February, ...
        Calendar startTime = Calendar.getInstance();
        startTime.set(2015,06,22,00,00);
        Calendar endTime= Calendar.getInstance();
        endTime.set(2015,06,23,00,00);

        // the range is all data from today to tomorrow
        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( "
                + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";


        Cursor cursor = this.getBaseContext().getContentResolver().query( CalendarContract.Events.CONTENT_URI, projection, selection, null, null );

        // output the events
        if (cursor.moveToFirst()) {
            do {
                //check event ID.
                Toast.makeText( this.getApplicationContext(), "Event " + cursor.getString(1) + " from Calendar " + cursor.getInt(0)
                        + " starting at " + (new Date(cursor.getLong(3))).toString(), Toast.LENGTH_LONG ).show();

                View event = getLayoutInflater().inflate(R.layout.todo_item, null);
                TextView todoTitle = (TextView)event.findViewById(R.id.todoTitle);
                TextView todoTime = (TextView)event.findViewById(R.id.todoTime);
                TextView todoLength = (TextView)event.findViewById(R.id.todoLength);

                todoTitle.setText(cursor.getString(1));
                todoTime.setText(new Date(cursor.getLong(3)).toString());
                todoLength.setText("ummm");

                todoLayout.addView(event);

            } while ( cursor.moveToNext());
        }
        cursor.close();
        //show Today's To Do List:






        /*
         List<Project> addableProjects = dbHandler.getAllProjects();
        int projectCount = dbHandler.getProjectCount();
        for (int i = 0; i < projectCount; i++){
            Projects.add(addableProjects.get(i));
        }

        //put project names into String
        if (!addableProjects.isEmpty()){
            String projectNames = "Your projects:";
            for (int i = 0; i < projectCount; i++){
                projectNames += " " + addableProjects.get(i).getName();
            }
            projectsMessage.setText(projectNames);
        }
         */

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
