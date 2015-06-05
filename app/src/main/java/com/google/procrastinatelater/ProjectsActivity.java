package com.google.procrastinatelater;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class ProjectsActivity extends Activity {

    EditText txtProjectTitle, txtTimeCmt, txtDue, txtHrsLong, txtMinsLong, txtFrq;
    List<Project> Projects = new ArrayList<Project>();
    ImageView projectNew1, project1Image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        /*Configuration configInfo = getResources().getConfiguration();
        if(configInfo.orientation == Configuration.ORIENTATION_LANDSCAPE){
            FragmentNewProject fragmentNewProject = new FragmentNewProject();
            fragmentTransaction.replace(android.R.id.content, fragmentNewProject);
        }else{
            FragmentExistingProject fragmentExistingProject = new FragmentExistingProject();
            fragmentTransaction.replace(android.R.id.content, fragmentExistingProject);
        }
        fragmentTransaction.commit();*/

        setContentView(R.layout.activity_projects);

        /*projectImageView = (ImageView) findViewById(R.id.projectImageView);
        projectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Project Image"), 1);
            }
        });*/
        txtProjectTitle = (EditText) findViewById(R.id.txtProjectTitle);
        txtTimeCmt = (EditText) findViewById(R.id.txtTimeCmt);
        //txtDue = (EditText) findViewById(R.id.txtDue);
        txtDue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DateDialog dialog = new DateDialog(v);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "DatePicker");
                }
            }
        });
        //txtHoursLong = (EditText) findViewById(R.id.txtHoursLong);
        txtMinsLong = (EditText) findViewById(R.id.txtMinsLong);
        txtFrq = (EditText) findViewById(R.id.txtFrq);

        Button btnCreate = (Button) findViewById(R.id.saveProjectButton);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //boolean is true when the field has been filled
                boolean title = !txtProjectTitle.getText().toString().trim().isEmpty();
                boolean cmt = !txtTimeCmt.getText().toString().trim().isEmpty();
                boolean due = !txtDue.getText().toString().trim().isEmpty();
                boolean howLong = !(txtHrsLong.getText().toString().trim().isEmpty() && txtMinsLong.getText().toString().trim().isEmpty());
                boolean frq = !txtFrq.getText().toString().trim().isEmpty();

                /**
                 * permit creation of new project on the following conditions
                 * all 4 options filled
                 * any combination of 3 filled
                 * howLong and frq filled
                 *
                 */
                if (title) {
                    if (howLong && frq) { //this conditions covers when all 4 are filled, and half of the 3-filled senarios
                        addProject(txtProjectTitle.getText().toString(), txtTimeCmt.getText().toString(), txtDue.getText().toString(), txtHrsLong.getText().toString(), txtMinsLong.getText().toString(), txtFrq.getText().toString());
                        Toast.makeText(getApplicationContext(), txtProjectTitle.getText().toString() + getString(R.string.project_created), Toast.LENGTH_SHORT).show();
                    } else if ((howLong || frq) && due && cmt) {
                        addProject(txtProjectTitle.getText().toString(), txtTimeCmt.getText().toString(), txtDue.getText().toString(), txtHrsLong.getText().toString(), txtMinsLong.getText().toString(), txtFrq.getText().toString());
                        Toast.makeText(getApplicationContext(), txtProjectTitle.getText().toString() + getString(R.string.project_created), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.project_missing_info), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.project_no_title), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /*
    request code
    result code
     */
    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1){
                //projectImageView.setImageURI(data.getData());
            }
        }
    }

    private void addProject(String projectName, String timeCmt, String dueDate, String sessionHrs, String sessionMins, String sessionFrq){
        //taken out!
        //Projects.add(new Project(projectName, timeCmt, dueDate, sessionHrs, sessionMins, sessionFrq));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}