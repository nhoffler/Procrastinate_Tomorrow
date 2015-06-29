package com.google.procrastinatelater;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ProjectsList extends Activity {
    //'physical' component variables
    ImageView projectImageView; //imageview of project we are viewing
    EditText txtProjectTitle, txtTimeCmt, txtDueDate, txtHrsLong, txtMinsLong, txtFrq; //text fields
    Button saveProjectButton, clearProjectButton; //buttons


    String projectImageUri = null; //current project's image path
    private static final Uri DEFAULT_URI = Uri.parse("android.resource://com.google.procrastinatelater/drawable/default_photo"); //default image

    //background and non-physical components
    FrameLayout newProjectFrame; //clicking this should empty out all fields
    DatabaseHandler dbHandler;
    ListView projectListView; //my scrolling list of existing projects
    List<Project> projectsList = new ArrayList<>(); //local list of projects. updated from database's copy.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.getGlobal().setLevel(Level.INFO);
        setContentView(R.layout.activity_projects_list);

        //set up sidebar
        //sidebar: new project icon
        newProjectFrame = (FrameLayout) findViewById(R.id.newProjectFrame);
        newProjectFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillFields(null);
            }
        });
        //sidebar: existing project icons
        projectListView = (ListView) findViewById(R.id.projectsListView);   //my scrolling list of icons
        dbHandler = new DatabaseHandler(getApplicationContext());   //make connection to database
        if (dbHandler.getProjectCount() != 0){
            projectsList.addAll(dbHandler.getAllProjects());    //add all existing projects to Projects list
        }
        populateList(); //set our projectListView's adapter, populate icons


        //identify physical components
        saveProjectButton = (Button) findViewById(R.id.saveProjectButton);  //create save button
        clearProjectButton = (Button) findViewById(R.id.clearProjectButton); //clear/delete project button
        txtProjectTitle = (EditText) findViewById(R.id.txtProjectTitle);    //project title
        txtTimeCmt = (EditText) findViewById(R.id.txtTimeCmt);              //time commitment
        txtHrsLong = (EditText) findViewById(R.id.txtHrsLong);              //session- # hours long
        txtMinsLong = (EditText) findViewById(R.id.txtMinsLong);            //session- # minutes long
        txtFrq = (EditText) findViewById(R.id.txtFrq);                      // session frequency
        projectImageView = (ImageView) findViewById(R.id.projectImageView); //project image
            //create Image chooser!
        projectImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 19){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 1);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 1);
                }
            }
        });
        txtDueDate = (EditText) findViewById(R.id.txtDueDate); //project due date
            //create Date Chooser
            //using onFocus to appear on first click
            //using onClick to edit the date a second time without returning focus
        txtDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog dialog = new DateDialog(v);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                dialog.show(ft, getString(R.string.select_due_date));
            }
        });
        txtDueDate.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DateDialog dialog = new DateDialog(v);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, getString(R.string.select_due_date));
                }
            }
        });

        fillFields(null); //make sure text fields are empty, set up button onClickListener
    }

    private boolean projectNameExists(Project project){
        String name = project.getName();
        int projectCount = projectsList.size();
        for (int i = 0; i < projectCount; i++){
            if (name.compareToIgnoreCase(projectsList.get(i).getName()) == 0){
                return true;
            }
        }
        return false;
    }

    private int findProjectIndex(Project aProject){
        long id = aProject.getId();
        int projectCount = projectsList.size();
        for (int i = 0; i < projectCount; i++){
            if (id == projectsList.get(i).getId()){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_projects_list, menu);
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

    /**
     * Called when user selects image and returns.
     * set the new image URI.
     *
     * @param reqCode (request) The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resCode (result) The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller
     */
    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1){
                projectImageUri = data.getData().toString();
                projectImageView.setImageURI(data.getData());
            }
        }
    }

    /**
     * Populate the ScrollView of projects on the left side of the screen
     */
    private void populateList(){
        ArrayAdapter<Project> adapter = new ProjectListAdapter();
        projectListView.setAdapter(adapter);
    }

    private class ProjectListAdapter extends ArrayAdapter<Project>{
        public ProjectListAdapter(){
            super(ProjectsList.this, R.layout.projects_list_item, projectsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){ //make view not null. inflate it
                convertView = getLayoutInflater().inflate(R.layout.projects_list_item, parent, false);
            }

            final Project currentProject = projectsList.get(position);

            //project image
            ImageView projectImage = (ImageView) convertView.findViewById(R.id.itemImage);
            //Logger.getLogger(getClass().getName()).info("Setting projectImageView image to " + imageUri);
            String imgUri = currentProject.getImgPath();
            if (imgUri != null){
                //TODO
                //projectImage.setImageURI(Uri.parse(imgUri));
            }else{
                projectImage.setImageURI(DEFAULT_URI);
            }
            if(projectImage.getDrawable() == null){
                //Logger.getLogger(getClass().getName()).log(Level.WARNING, "setting img url to default");
                projectImage.setImageURI(DEFAULT_URI);
            }

            //project title
            TextView projectTitle = (TextView) convertView.findViewById(R.id.titleHere);
            projectTitle.setText(currentProject.getName());

            FrameLayout itemFrame = (FrameLayout) convertView.findViewById(R.id.itemFrame);
            itemFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fillFields(currentProject);
                }
            });
            return convertView;
        }
    }

    public void fillFields(Project project){
        if (project == null){
            //set default image
            projectImageUri = null;
            projectImageView.setImageURI(DEFAULT_URI);
            txtProjectTitle.setText("");
            txtTimeCmt.setText("");
            txtDueDate.setText("");
            txtHrsLong.setText("");
            txtMinsLong.setText("");
            txtFrq.setText("");
            //buttons: Create and Clear
            buttonToCreate();
            buttonToClear();

        }else {
            //set project image

            String imageUri = project.getImgPath();
            if (imageUri != null) {
                Logger.getLogger(getClass().getName()).info("Setting projectImageView image to " + imageUri);
                projectImageView.setImageURI(Uri.parse(imageUri));
            }else{
                projectImageView.setImageURI(DEFAULT_URI);
            }

            if(projectImageView.getDrawable() == null){
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "setting img url to default");
                projectImageView.setImageURI(DEFAULT_URI);
                Toast.makeText(getApplicationContext(), getString(R.string.lost_image), Toast.LENGTH_SHORT).show();
            }

            //fill text fields
            txtProjectTitle.setText(project.getName());
            txtTimeCmt.setText(project.getCmt());
            txtDueDate.setText(project.getDueDate());
            txtHrsLong.setText(project.getSnHrs());
            txtMinsLong.setText(project.getSnMins());
            txtFrq.setText(project.getSnFrq());
            //buttons: Update and Delete
            buttonToUpdate(project);
            buttonToDelete(project);
        }
    }

    /**
     * save project button should say "create" and
     * add a new project to the database and the projects scroll
     */
    private void buttonToCreate(){
        saveProjectButton.setText(getString(R.string.create_project));
        saveProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //collect project title
                String title = String.valueOf(txtProjectTitle.getText());
                //boolean is true when title is not empty.
                boolean b_title = !title.trim().isEmpty();

                if (b_title){
                    //collect values
                    String cmt = String.valueOf(txtTimeCmt.getText());
                    String due = String.valueOf(txtDueDate.getText());
                    String hrs = String.valueOf(txtHrsLong.getText());
                    String mins = String.valueOf(txtMinsLong.getText());
                    String frq = String.valueOf(txtFrq.getText());

                    //boolean is true when the field has been filled. using trim() to take out empty spaces.
                    boolean b_cmt = !cmt.trim().isEmpty();
                    boolean b_due = !due.trim().isEmpty();
                    boolean b_howLong = !(hrs.trim().isEmpty() && mins.trim().isEmpty());
                    boolean b_frq = !frq.trim().isEmpty();

                    if ((b_howLong && b_frq)|| ((b_howLong || b_frq) && b_due && b_cmt)){ //this conditions covers when all 4 are filled, and half of the 3-filled senarios
                        Project project = new Project(0, title, cmt, due, hrs, mins, frq, projectImageUri);
                        dbHandler.createProject(project);
                        projectsList.add(project);
                        populateList();
                        fillFields(project);
                        Toast.makeText(getApplicationContext(), title + " " + getString(R.string.project_created), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), dbHandler.getProjectCount() + " projects!", Toast.LENGTH_SHORT).show();

                        createEvent(project);

                    } else { //message: not enough project info
                        Toast.makeText(getApplicationContext(), getString(R.string.project_missing_info), Toast.LENGTH_SHORT).show();
                    }
                } else { //message: no title
                    Toast.makeText(getApplicationContext(), getString(R.string.project_no_title), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buttonToClear(){
        clearProjectButton.setText(getString(R.string.clear_fields));
        clearProjectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                projectImageUri = null;
                projectImageView.setImageURI(DEFAULT_URI);
                txtProjectTitle.setText("");
                txtTimeCmt.setText("");
                txtDueDate.setText("");
                txtHrsLong.setText("");
                txtMinsLong.setText("");
                txtFrq.setText("");
            }
        });
    }

    /**
     * save project button should say "update" and
     * update an existing project in the database and the projects scroll
     */
    private void buttonToUpdate(final Project aProject){
        saveProjectButton.setText(getString(R.string.update_project));
        saveProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //collect project title
                String title = String.valueOf(txtProjectTitle.getText());
                //boolean is true when title is not empty.
                boolean b_title = !title.trim().isEmpty();

                if (b_title){
                    //collect values
                    String cmt = String.valueOf(txtTimeCmt.getText());
                    String due = String.valueOf(txtDueDate.getText());
                    String hrs = String.valueOf(txtHrsLong.getText());
                    String mins = String.valueOf(txtMinsLong.getText());
                    String frq = String.valueOf(txtFrq.getText());

                    //boolean is true when the field has been filled. using trim() to take out empty spaces.
                    boolean b_cmt = !cmt.trim().isEmpty();
                    boolean b_due = !due.trim().isEmpty();
                    boolean b_howLong = !(hrs.trim().isEmpty() && mins.trim().isEmpty());
                    boolean b_frq = !frq.trim().isEmpty();

                    if ((b_howLong && b_frq)|| ((b_howLong || b_frq) && b_due && b_cmt)){ //this conditions covers when all 4 are filled, and half of the 3-filled senarios
                        aProject.setName(title);
                        aProject.setCmt(cmt);
                        aProject.setDueDate(due);
                        aProject.setSnHrs(hrs);
                        aProject.setSnMins(mins);
                        aProject.setSnFrq(frq);
                        aProject.setImgPath(projectImageUri);

                        dbHandler.updateProject(aProject);
                        int projectIndex = findProjectIndex(aProject);
                        //TODO
                        //try catch??
                        if (projectIndex != -1){
                            projectsList.set(projectIndex, aProject);
                        }
                        populateList();
                        //Toast.makeText(getApplicationContext(), title + " " + getString(R.string.project_updated), Toast.LENGTH_SHORT).show();

                    } else { //message: not enough project info
                        Toast.makeText(getApplicationContext(), getString(R.string.project_missing_info), Toast.LENGTH_SHORT).show();
                    }
                } else { //message: no title
                    Toast.makeText(getApplicationContext(), getString(R.string.project_no_title), Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    /**
     * clear button should say "Delete" and
     * delete the existing project from the database and the projects scroll
     */
    private void buttonToDelete(final Project aProject){
        clearProjectButton.setText(getString(R.string.delete_project));
        clearProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.deleteProject(aProject);
                projectsList.remove(aProject);
                populateList();
                fillFields(null);
                Toast.makeText(getApplicationContext(), aProject.getName() + " " + getString(R.string.project_deleted), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void createEvent(Project aProject){
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("title", aProject.getName());
        intent.putExtra("description", "This is a Procrastinate Tomorrow session.");
        intent.putExtra("allDay", false);
        intent.putExtra("beginTime", cal.getTimeInMillis());

        String strCmt = aProject.getCmt();
        String strDue = aProject.getDueDate();
        String strHrs = aProject.getSnHrs();
        String strMins = aProject.getSnMins();
        String strFrq = aProject.getSnFrq();
        //boolean is true when the field has been filled. using trim() to take out empty spaces.
        boolean b_cmt = !strCmt.trim().isEmpty();
        boolean b_due = !strDue.trim().isEmpty();
        boolean b_howLong = !(strHrs.trim().isEmpty() && strMins.trim().isEmpty());
        boolean b_frq = !strFrq.trim().isEmpty();

        if (!b_cmt && !b_due && b_howLong && b_frq){ //if we know only session frequency and length

            intent.putExtra("endTime", cal.getTimeInMillis()+putSessionLength(strHrs, strMins));
            intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq));


        }else if (!b_cmt && b_due && b_howLong && b_frq){ //we know session frequency and length, and when the project is due

            intent.putExtra("endTime", cal.getTimeInMillis()+putSessionLength(strHrs, strMins));
            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            String untilDate = putEndDate(stringToCalendar(strDue));
            if (untilDate != null){
                //Toast.makeText(getApplicationContext(), "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq));
            }


        }else if (b_cmt && !b_due && b_howLong && b_frq){ //we know time commitment, and session length and frequency

            float sessionInMs = putSessionLength(strHrs, strMins);
            float projectCmt = Float.parseFloat(strCmt);

            intent.putExtra("endTime", cal.getTimeInMillis() +
                    putSessionLength(strHrs, strMins)); //not using float sessionInMs because of calendar bug
            intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq) +
                    ";COUNT=" + putCountFromCmt(projectCmt, sessionInMs));


        }else if (b_cmt && b_due && !b_howLong && b_frq){   //we know time commitment, the due date, and session frequency

            Calendar endCalendar = stringToCalendar(strDue);

            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            String untilDate = putEndDate(endCalendar);
            if (untilDate != null){
                //Toast.makeText(getApplicationContext(), "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq));
            }

            //put session length
            intent.putExtra("endTime", cal.getTimeInMillis() + putLengthFromCmt(strCmt, strFrq, cal, endCalendar));


        }else if (b_cmt && b_due && b_howLong && !b_frq){ //we know time commitment, the due date, and session length

            int sessionInMs = putSessionLength(strHrs, strMins);
            Calendar endCalendar = stringToCalendar(strDue);
            String untilDate = putEndDate(endCalendar);
            String sessionFrq = putFrqFromCmt(strCmt, sessionInMs, cal, endCalendar);

            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            if (untilDate != null){
                //Toast.makeText(getApplicationContext(), "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(sessionFrq) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(sessionFrq));
            }

            //put session length
            intent.putExtra("endTime", cal.getTimeInMillis()+sessionInMs);


        }else if (b_cmt && b_due && b_howLong && b_frq){

            //fill in due date, session length, and session frequency

            int sessionInMs = putSessionLength(strHrs, strMins);
            float projectCmt = Float.parseFloat(strCmt);
            Calendar endCalendar = stringToCalendar(strDue);
            String untilDate = putEndDate(endCalendar);

            intent.putExtra("endTime", cal.getTimeInMillis() + putSessionLength(strHrs, strMins)); //not using float sessionInMs because of calendar bug

            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            if (untilDate != null){
                //Toast.makeText(getApplicationContext(), "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq) + ";COUNT=" + putCountFromCmt(projectCmt, sessionInMs) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + putSessionFrequency(strFrq)+ ";COUNT=" + putCountFromCmt(projectCmt, sessionInMs));
            }

            //check due date.
            int tentativeFrq = Integer.parseInt(putFrqFromCmt(strCmt, sessionInMs, cal, endCalendar));
            if (tentativeFrq > Integer.parseInt(strFrq)){
                Toast.makeText(getApplicationContext(), "Warning: Given these criteria, you will not finish this project on time." , Toast.LENGTH_LONG).show();
            }

        }

        startActivity(intent);
    }


    private String putFrqFromCmt(String aCmt, int aSessionInMs, Calendar aStart, Calendar anEnd){
        String projectFrq;
        float projectCmt = Float.parseFloat(aCmt);
        float sessionInHrs = (float)aSessionInMs/(1000*60*60);
        //Toast.makeText(getApplicationContext(), "Length in Hrs: " + sessionInHrs, Toast.LENGTH_SHORT).show();
        int numSessions = (int)Math.ceil(projectCmt/sessionInHrs);
        //Toast.makeText(getApplicationContext(), "Number of Session Needed: " + numSessions, Toast.LENGTH_SHORT).show();


        int[] numDays = new int[7]; //represents the number of each day of the week, Sundays (0) through Saturdays (6)


        for (Calendar loopCal = Calendar.getInstance(); loopCal.before(anEnd); loopCal.add(Calendar.DATE, 1)) {
            //Calendar.Sunday == 1 and Calendar.SATURDAY == 7
            //numDays[0] == Sunday and numDays[6] == Saturday
            int thisDayOfWeek = loopCal.get(Calendar.DAY_OF_WEEK)-1;
            numDays[thisDayOfWeek] += 1;
        }

        int dayOfWeekToday = aStart.get(Calendar.DAY_OF_WEEK)-1;
        //if the number of Mondays, Tuesdays, etc between now and the due date is greater than the number of sessions needed, repeat those days
        if (numDays[dayOfWeekToday] >= numSessions){
            projectFrq = "1"; //starting today, we can repeat one day a week on every dayOfWeekToday
        }else if(numDays[1]+numDays[2] >= numSessions){ //Monday and Tuesday
            projectFrq = "2";
        }else if(numDays[1]+numDays[2]+numDays[3] >= numSessions){ //Monday through Wednesday
            projectFrq = "3";
        }else if(numDays[1]+numDays[2]+numDays[3]+numDays[4] >= numSessions){ //Monday through Thursday
            projectFrq = "4";
        }else if(numDays[1]+numDays[2]+numDays[3]+numDays[4]+numDays[5] >= numSessions){ //Monday through Friday
            projectFrq = "5";
        }else if(numDays[1]+numDays[2]+numDays[3]+numDays[4]+numDays[5]+numDays[6] >= numSessions){ //Monday through Saturday
            projectFrq = "6";
        }else if(numDays[1]+numDays[2]+numDays[3]+numDays[4]+numDays[5]+numDays[6]+numDays[0] >= numSessions){ //Monday through Sunday
            projectFrq = "7";
        }else{
            projectFrq = "8";
            Toast.makeText(getApplicationContext(), "Warning: You will not finish this project on time without increasing your session length." , Toast.LENGTH_LONG).show();
        }

        return projectFrq;
    }

    private int putLengthFromCmt(String aCmt, String aFrq, Calendar aStart, Calendar anEnd){
        //divide the time commitment by the number of sessions between now and the due date

        //step one: find number of sessions
        int numSessions = 0;
        int dayOfWeekToday = aStart.get(Calendar.DAY_OF_WEEK);

        for (Calendar loopCal = Calendar.getInstance(); loopCal.before(anEnd); loopCal.add(Calendar.DATE, 1)) {

            int sessionFrq = Integer.parseInt(aFrq); //how often user has chosen to work per week
            if (sessionFrq == 1){ //if weekly sessions, beginning today:
                if (loopCal.get(Calendar.DAY_OF_WEEK) == dayOfWeekToday){ //if this day is the same day of the week as today
                    numSessions++;
                }
            }else if (sessionFrq >= 7){ //daily sessions, count every day
                numSessions++;
            }else{ //depends on what days user has sessions
                //Calendar.MONDAY == 2 and Calendar.SATURDAY == 7
                //sessionFrq 2 == Monday(2) thru Tuesday(3)
                //sesisonFrq 6 == Monday(2) thru Saturday(7)
                if (loopCal.get(Calendar.DAY_OF_WEEK) >= 2 && loopCal.get(Calendar.DAY_OF_WEEK) <= (sessionFrq+1) ){ //if this day falls on a session day, Monday through XXX
                    numSessions++;
                }
            }
        }
        //Toast.makeText(getApplicationContext(), "numSessions" + numSessions, Toast.LENGTH_SHORT).show();

        //step two: divide cmt into number of sessions to find time per session
        Float sessionLengthInHrs = (Float.parseFloat(aCmt)/numSessions);
        //Toast.makeText(getApplicationContext(), "Session Length: " + sessionLengthInHrs, Toast.LENGTH_SHORT).show();
        int sessionLengthInMs = (int)(1000*60*60*sessionLengthInHrs);

        return sessionLengthInMs;
    }

    /**
     *
     * @param aCmt time needed to complete project; its time commitment
     * @param aSessionInMs length of sessions IN MILLISECONDS
     * @return
     */
    private int putCountFromCmt(float aCmt, float aSessionInMs){
        float sessionInHrs = aSessionInMs/(1000*60*60); //calculate session in hours
        int numSessions = (int)Math.ceil(aCmt/sessionInHrs); //total time divided by session length = number of session needed
        //Toast.makeText(getApplicationContext(), "" + aCmt + "/" + sessionInHrs + "= " + (hrsTotal/sessionInHrs) , Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "numSessions" + numSessions, Toast.LENGTH_SHORT).show();
        return numSessions;
    }

    private Calendar stringToCalendar(String aDateString){
        Calendar myCal = Calendar.getInstance();
        SimpleDateFormat formatIn = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

        try {

            Date projectDate = formatIn.parse(aDateString.toLowerCase()); //use format to parse parameter into Date projectDate
            myCal.setTime(projectDate);
        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), "We were unable use your due date", Toast.LENGTH_SHORT).show();
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "could not parse due date");
            //e.printStackTrace();
            return null;
        }

        return myCal;
    }

    private String putEndDate(Calendar anEndCal){

        if (anEndCal == null){
            return null;
        }

        String rfcDate;
        int year = anEndCal.get(Calendar.YEAR);
        int month = anEndCal.get(Calendar.MONTH)+1; //months start at 0 instead of 1
        int day = anEndCal.get(Calendar.DAY_OF_MONTH);
        //int[] dateArray = new int[]{year, month+1, day};  //months start at 0 instead of 1!

        rfcDate = String.valueOf(year);
        if (month < 10){
            rfcDate += "0"; //the month should be two digits. ex: 01-12
        }
        rfcDate += month;
        if (day < 10){
            rfcDate += "0"; //the day should be two digits. ex: 01-12
        }
        rfcDate += day;
        rfcDate += "T235959"; //midnight.

        return rfcDate;
    }


    private int putSessionLength(String aStrHrs, String aStrMins){
        int hrs = 0;
        int mins = 0;
        if (!aStrHrs.trim().isEmpty()){
            hrs = Integer.parseInt(aStrHrs);
        }
        if (!aStrMins.trim().isEmpty()){
            mins = Integer.parseInt(aStrMins);
        }
        hrs = hrs*1000*60*60;
        mins = mins*1000*60;
        return hrs+mins;
    }

    private String putSessionFrequency(String aStrFrq){
        String frqParam;
        int frq = Integer.parseInt(aStrFrq);
        switch (frq){
            case 1:
                frqParam = "WEEKLY";
                break;
            case 2:
                frqParam = "WEEKLY;BYDAY=MO,TU";
                break;
            case 3:
                frqParam = "WEEKLY;BYDAY=MO,TU,WE";
                break;
            case 4:
                frqParam = "WEEKLY;BYDAY=MO,TU,WE,TH";
                break;
            case 5:
                frqParam = "WEEKLY;BYDAY=MO,TU,WE,TH,FR";
                break;
            case 6:
                frqParam = "WEEKLY;BYDAY=MO,TU,WE,TH,FR,SA";
                break;
            default:
                frqParam = "DAILY";
                break;
        }
        return frqParam;
    }







































}
