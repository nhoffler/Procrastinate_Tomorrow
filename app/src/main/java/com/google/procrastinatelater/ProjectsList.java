package com.google.procrastinatelater;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ProjectsList extends Activity {
    //physical components' variables
    FrameLayout newProjectFrame; //clicking this should empty out all fields
    EditText txtProjectTitle, txtTimeCmt, txtDueDate, txtHrsLong, txtMinsLong, txtFrq; //text fields
    Button saveProjectButton; //will change between Create Project and Update Project

    ImageView projectImageView; //imageview of project we are viewing
    String projectImagePath = null; //current project's image path
    Bitmap myBitmap = null; //supposedly used to create my images from their paths
    private static final Uri DEFAULT_URI = Uri.parse("android.resource://com.google.procrastinatelater/drawable/default_photo.jpg"); //default image

    //background and non-physical components
    List<Project> Projects = new ArrayList<Project>(); //local list of projects. updated from database's copy. Is this a necessary variable?
    DatabaseHandler dbHandler;
    ListView projectListView; //my scrolling list of existing projects



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Projects.addAll(dbHandler.getAllProjects());    //add all existing projects to Projects list
        }
        populateList(); //set our projectListView's adapter, populate icons


        //identify physical components
        saveProjectButton = (Button) findViewById(R.id.saveProjectButton);  //create save button
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
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 1);
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

    private boolean projectExists(Project project){
        String name = project.getName();
        int projectCount = Projects.size();
        for (int i = 0; i < projectCount; i++){
            if (name.compareToIgnoreCase(Projects.get(i).getName()) == 0){
                return true;
            }
        }
        return false;
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
                projectImagePath = data.getData().getPath();
                //projectImageView.setImageURI(
                findMyImage(projectImagePath, projectImageView);
                //projectImageView.setImageURI(data.getData());
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
            super(ProjectsList.this, R.layout.projects_list_item, Projects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){ //make view not null. inflate it
                convertView = getLayoutInflater().inflate(R.layout.projects_list_item, parent, false);
            }

            final Project currentProject = Projects.get(position);
            //project image
            ImageView projectImage = (ImageView) convertView.findViewById(R.id.itemImage);
            //Uri projectUri =
            //--findUri(currentProject.getImgPath(), projectImage); //findUri, a method inside ProjectsList.java
            //projectImage.setImageURI(projectUri);
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
            //findMyImage(null, projectImageView);
            txtProjectTitle.setText("");
            txtTimeCmt.setText("");
            txtDueDate.setText("");
            txtHrsLong.setText("");
            txtMinsLong.setText("");
            txtFrq.setText("");
            saveProjectButton.setText(getString(R.string.create_project));
            saveProjectButton.setOnClickListener(createOnClick());
        }else {
            //skipped image!
            txtProjectTitle.setText(project.getName());
            txtTimeCmt.setText(project.getCmt());
            txtDueDate.setText(project.getDate());
            txtHrsLong.setText(project.getSnHrs());
            txtMinsLong.setText(project.getSnMins());
            txtFrq.setText(project.getSnFrq());
            saveProjectButton.setText(getString(R.string.update_project));
            //saveProjectButton.setOnClickListener(updateOnClick());
        }
    }

    private void findMyImage(String imgPath, ImageView imgView){
        Uri imgUri = DEFAULT_URI;
        File imgFile = new File(imgPath);
        if(imgFile.exists())
        {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgView.setImageBitmap(myBitmap);
            //imgUri = Uri.fromFile(imgFile);
        }else{
            //imgView.setImageURI(DEFAULT_URI);
        }
    }

    /**
     * for creating a new project and saving it to the database
     * @return an onClickListener for our Create Project button
     */
    private View.OnClickListener createOnClick(){
        View.OnClickListener listenToCreate = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //collect values
                String title = String.valueOf(txtProjectTitle.getText());
                String cmt = String.valueOf(txtTimeCmt.getText());
                String due = String.valueOf(txtDueDate.getText());
                String hrs = String.valueOf(txtHrsLong.getText());
                String mins = String.valueOf(txtMinsLong.getText());
                String frq = String.valueOf(txtFrq.getText());
                //boolean is true when the field has been filled. using trim() to take out empty spaces.
                boolean b_title = !title.trim().isEmpty();
                boolean b_cmt = !cmt.trim().isEmpty();
                boolean b_due = !due.trim().isEmpty();
                boolean b_howLong = !(hrs.trim().isEmpty() && mins.trim().isEmpty());
                boolean b_frq = !frq.trim().isEmpty();

                if (b_title){
                    if (b_howLong && b_frq) { //this conditions covers when all 4 are filled, and half of the 3-filled senarios
                        Project project = new Project(dbHandler.getProjectCount(), title, cmt, due, hrs, mins, frq, projectImagePath);
                        dbHandler.createProject(project);
                        Projects.add(project);
                        populateList();
                        Toast.makeText(getApplicationContext(), title + " " + getString(R.string.project_created), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), dbHandler.getProjectCount() + " projects!", Toast.LENGTH_SHORT).show();
                    } else if ((b_howLong || b_frq) && b_due && b_cmt) { //covers other two 3-filled senarios
                        Project project = new Project(dbHandler.getProjectCount(), title, cmt, due, hrs, mins, frq, projectImagePath);
                        dbHandler.createProject(project);
                        Projects.add(project);
                        populateList();
                        Toast.makeText(getApplicationContext(), title + " " + getString(R.string.project_created), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), dbHandler.getProjectCount() + " projects!", Toast.LENGTH_SHORT).show();
                    } else { //message: not enough project info
                        Toast.makeText(getApplicationContext(), getString(R.string.project_missing_info), Toast.LENGTH_SHORT).show();
                    }
                } else { //message: no title
                    Toast.makeText(getApplicationContext(), getString(R.string.project_no_title), Toast.LENGTH_SHORT).show();
                }


            }
        };
        return listenToCreate;
    }

    /**
     * for updating an existing project
     * @return an onClickListener for our Update Project button
     */
    private View.OnClickListener updateOnClick(){
        View.OnClickListener listenToUpdate = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //collect values
                String title = String.valueOf(txtProjectTitle.getText());
                String cmt = String.valueOf(txtTimeCmt.getText());
                String due = String.valueOf(txtDueDate.getText());
                String hrs = String.valueOf(txtHrsLong.getText());
                String mins = String.valueOf(txtMinsLong.getText());
                String frq = String.valueOf(txtFrq.getText());
                //boolean is true when the field has been filled. using trim() to take out empty spaces.
                boolean b_title = !title.trim().isEmpty();
                boolean b_cmt = !cmt.trim().isEmpty();
                boolean b_due = !due.trim().isEmpty();
                boolean b_howLong = !(hrs.trim().isEmpty() && mins.trim().isEmpty());
                boolean b_frq = !frq.trim().isEmpty();

                if (b_title){
                    if (b_howLong && b_frq) { //this conditions covers when all 4 are filled, and half of the 3-filled senarios
                        Project project = new Project(dbHandler.getProjectCount(), title, cmt, due, hrs, mins, frq, projectImagePath);
                        dbHandler.updateProject(project);
                        //Projects.remove(project);
                        //Projects.add(project);
                        //populateList();
                        Toast.makeText(getApplicationContext(), title + " " + getString(R.string.project_updated), Toast.LENGTH_SHORT).show();
                    } else if ((b_howLong || b_frq) && b_due && b_cmt) { //covers other two 3-filled senarios
                        Project project = new Project(dbHandler.getProjectCount(), title, cmt, due, hrs, mins, frq, projectImagePath);
                        dbHandler.updateProject(project);
                        //Projects.add(project);
                        //populateList();
                        Toast.makeText(getApplicationContext(), title + " " + getString(R.string.project_updated), Toast.LENGTH_SHORT).show();
                    } else { //message: not enough project info
                        Toast.makeText(getApplicationContext(), getString(R.string.project_missing_info), Toast.LENGTH_SHORT).show();
                    }
                } else { //message: no title
                    Toast.makeText(getApplicationContext(), getString(R.string.project_no_title), Toast.LENGTH_SHORT).show();
                }


            }
        };
        return listenToUpdate;
    }

}
