package com.google.procrastinatelater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class HomeActivity extends Activity {
    LinearLayout toProjectsLayout, toCalendarLayout;
    LinearLayout todoLayout;
    TextView projectsMessage; //message is removed from todoLayout, and replaces the layout in todoFrame if the layout is empty
    DatabaseHandler dbHandler;
    private static final Uri DEFAULT_URI = Uri.parse("android.resource://com.google.procrastinatelater/drawable/default_photo"); //default image

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

        //get list of all Procrastinate Later projects
        dbHandler = new DatabaseHandler(getApplicationContext());
        List<Project> proProjects = dbHandler.getAllProjects();
        ArrayList<Long> eventIds = new ArrayList<Long>();
        int projectCount = dbHandler.getProjectCount();
        String ids = "";
        for (int i = 0; i < projectCount; i++){
            eventIds.add(proProjects.get(i).getEventId());
            //ids += proProjects.get(i).getEventId() + " ";
        }
        //Toast.makeText( this.getApplicationContext(), "Projects: " + ids, Toast.LENGTH_LONG ).show();




        //Populate To Do List
        todoLayout.removeAllViews(); //clear out old to do
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

        //'sql' here
        String[] projection = new String[] {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY};
        //set today's 12:00am to 11:59pm
        Calendar startToday = Calendar.getInstance();
        startToday.set(Calendar.HOUR_OF_DAY, 0);
        startToday.set(Calendar.MINUTE, 0);
        startToday.set(Calendar.SECOND, 0);
        Calendar endToday= Calendar.getInstance();
        endToday.set(Calendar.HOUR_OF_DAY, 23);
        endToday.set(Calendar.MINUTE, 59);
        endToday.set(Calendar.SECOND, 59);

        // the range is all data from today to tomorrow
        String selection = "(( " + CalendarContract.Instances.BEGIN + " >= " + startToday.getTimeInMillis() + " ) AND ( "
                + CalendarContract.Instances.BEGIN + " <= " + endToday.getTimeInMillis() + " ))";


        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, startToday.getTimeInMillis());
        ContentUris.appendId(eventsUriBuilder, endToday.getTimeInMillis());
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = this.getBaseContext().getContentResolver().query(eventsUri, projection, null, null, CalendarContract.Instances.BEGIN + " ASC");

        // output the events
        if (cursor.moveToFirst()) {
            do {

                //Toast.makeText( this.getApplicationContext(), "Event " + cursor.getString(1) + " all day "
                //       + cursor.getString(4) + " starting at " + (new Date(cursor.getLong(3))).toString(), Toast.LENGTH_LONG ).show();

                Date startDate = new Date(cursor.getLong(2));
                Date endDate = new Date(cursor.getLong(3));

                //check event ID.
                int eventIndex = eventIds.indexOf(cursor.getLong(0)); //index of event in proProjects. -1 if this event is not from Procrastinate Later
                if (eventIndex > -1){ //if this is a Pro Later event,

                    //create to do view, set its event's text field values
                    View event = getLayoutInflater().inflate(R.layout.todo_item, null);
                    TextView todoTitle = (TextView)event.findViewById(R.id.todoTitle);
                    TextView todoTime = (TextView)event.findViewById(R.id.todoTime);
                    TextView todoLength = (TextView)event.findViewById(R.id.todoLength);
                    ImageView todoImg = (ImageView)event.findViewById(R.id.todoImg);

                    //project image
                    String imgUri = proProjects.get(eventIndex).getImgPath();
                    if (imgUri != null){
                        try {
                            int iconWidth = 300;
                            Bitmap projectBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imgUri));
                            todoImg.setImageBitmap(Bitmap.createScaledBitmap(projectBitmap, iconWidth, iconWidth, true));

                        } catch (Exception e) {
                            todoImg.setImageURI(DEFAULT_URI);
                        }
                    }else{
                        todoImg.setImageURI(DEFAULT_URI);
                    }
                    if(todoImg.getDrawable() == null){
                        //Logger.getLogger(getClass().getName()).log(Level.WARNING, "setting img url to default");
                        todoImg.setImageURI(DEFAULT_URI);
                    }



                    if (cursor.getInt(4) == 0){ //if this is NOT an all day event

                        long duration = endDate.getTime()-startDate.getTime();
                        int hrs = (int)(duration/(1000*60*60));
                        int mins = (int) (duration - (1000*60*60*hrs))/(1000*60);
                        String dur = "";
                        if (hrs > 0){
                            dur += (hrs + " hrs ");
                        }
                        if (mins > 0){
                            dur += (mins + " mins");
                        }

                        todoTitle.setText(cursor.getString(1)); //event title
                        todoTime.setText(dateFormat.format(startDate)); //starting time
                        todoLength.setText(dur); //duration
                        todoLayout.addView(event);  //add to To do list.

                    }else if (!startDate.after(startToday.getTime())){   //this is an all day event Correctly Pulled for today

                        todoTitle.setText(cursor.getString(1)); //event title
                        todoLength.setText(R.string.all_day); // say "all day" instead of number of hours
                        todoTime.setText("");
                        todoLayout.addView(event);  //add to To do list.

                    }
                    /*else{ //this is an all day event for tomorrow, most likely

                        //do nothing.
                        todoTitle.setText(cursor.getString(1)); //event title
                        todoLength.setText("this event is for tomorrow!");
                        todoTime.setText("");
                    }*/
                }

            } while ( cursor.moveToNext());

        }
        if (todoLayout.getChildCount() == 0){   //if there are no sessions today,
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
