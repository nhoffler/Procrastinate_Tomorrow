package com.google.procrastinatelater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
        //populate Today's Projects list
        dbHandler = new DatabaseHandler(getApplicationContext());
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

    public void goToProjects(View view) {
        Intent getProjectsScreenIntent = new Intent(this, ProjectsList.class);
        final int result = 1;
        //how to make Android Apps 5 Derek Banas. 14 minutes
        // to pass data
        // getProjectsScreenIntent.putExtra("string's name", "string data to pass over");

        startActivity(getProjectsScreenIntent);
        //startActivityForResult(getProjectsScreenIntent, result);

    }

    public void goToCalendar(View view) {
        //old code to show self-made calendar
        //Intent getCalendarScreenIntent = new Intent(this, CalendarActivity.class);
        //startActivity(getCalendarScreenIntent);

        //new code to user's calendar
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
}
