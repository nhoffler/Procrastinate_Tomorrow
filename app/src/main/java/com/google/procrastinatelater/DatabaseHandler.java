package com.google.procrastinatelater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Nicole on 20-May-15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "projectsDatabase", //database
            TABLE_PROJECTS = "projectsTable", //projects table
            KEY_ID = "id", //project id. autoincrements
            KEY_PROJECTNAME = "projectTitle", //project title
            KEY_TIMECMT = "timeCmt", //time commitment
            KEY_DUEDATE = "dueDate", //due date
            KEY_HRSLONG = "hrsLong", //session length in hours
            KEY_MINSLONG = "minsLong", //session length in minutes
            KEY_FRQ = "frq", //session frequency
            KEY_IMGPATH = "imgPath", //image path...
            KEY_EVENTID = "eventID"; //id of event in Android calendar

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.
     * The database is not actually created or opened until one of getWritableDatabase() or getReadableDatabase() is called.
     * @param context to use to open or create the database
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //CREATE DATABASE db...
        db.execSQL("CREATE TABLE " + TABLE_PROJECTS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PROJECTNAME + " TEXT, " +
                KEY_TIMECMT + " TEXT, " +
                KEY_DUEDATE + " TEXT, " +
                KEY_HRSLONG + " TEXT, " +
                KEY_MINSLONG + " TEXT, " +
                KEY_FRQ + " TEXT, " +
                KEY_IMGPATH + " TEXT, " +
                KEY_EVENTID + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        onCreate(db);
    }

    //CRUD: create, read, update, delete

    /**
     * insert a project into the database
     * @param project project to insert
     */
    public long createProject(Project project) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROJECTNAME, project.getName());
        values.put(KEY_TIMECMT, project.getCmt());
        values.put(KEY_DUEDATE, project.getDueDate());
        values.put(KEY_HRSLONG, project.getSnHrs());
        values.put(KEY_MINSLONG, project.getSnMins());
        values.put(KEY_FRQ, project.getSnFrq());
        values.put(KEY_IMGPATH, project.getImgPath());
        values.put(KEY_EVENTID, project.getEventId());

        long generatedKey = db.insert(TABLE_PROJECTS, null, values);
        project.setId(generatedKey);
        db.close();
        Logger.getLogger(getClass().getName()).info("Saved "
                + project.getName() + " project to database with image "
                + project.getImgPath());

        return generatedKey;
    }

    /**
     * return a project, given its id
     * @param id the project's unique id
     * @return project with that id. Project does not exist if returns null.
     */
    public Project getProject(long id) {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = new String[]{KEY_ID, KEY_PROJECTNAME, KEY_TIMECMT, KEY_DUEDATE, KEY_HRSLONG,
                KEY_MINSLONG, KEY_TIMECMT, KEY_IMGPATH, KEY_EVENTID};
        Cursor cursor = db.query(TABLE_PROJECTS, columns, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        Project project = new Project(Long.parseLong(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6), cursor.getString(7), Long.parseLong(cursor.getString(8)));

        cursor.close();
        db.close();
        return project;
    }

    /**
     * update given project
     * @param project project to update
     * @return number of rows affected in the database
     */
    public int updateProject(Project project){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROJECTNAME, project.getName());
        values.put(KEY_TIMECMT, project.getCmt());
        values.put(KEY_DUEDATE, project.getDueDate());
        values.put(KEY_HRSLONG, project.getSnHrs());
        values.put(KEY_MINSLONG, project.getSnMins());
        values.put(KEY_FRQ, project.getSnFrq());
        values.put(KEY_IMGPATH, project.getImgPath());
        values.put(KEY_EVENTID, project.getEventId());

        int rowsAffected = db.update(TABLE_PROJECTS, values, KEY_ID + "=?", new String[]{String.valueOf(project.getId())});
        db.close();
        return rowsAffected;
    }

    /**
     * delete a project
     * @param project project to be deleted
     */
    public void deleteProject(Project project){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_PROJECTS, KEY_ID + " =?", new String[]{String.valueOf(project.getId())});
        db.close();
    }
    //end CRUD

    /**
     * @return number of projects in the Projects table
     */
    public int getProjectCount(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROJECTS, null);
        int numProjects = cursor.getCount();

        cursor.close();
        db.close();
        return numProjects;
    }

    /**
     * @return a list of all projects in the Projects table
     */
    public List<Project> getAllProjects(){
        List<Project> projects = new ArrayList<Project>();
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROJECTS, null);
        if (cursor.moveToFirst()){
            do{
                Long myEventId = Long.valueOf(-1);
                if (cursor.getString(8) != null){
                    myEventId = Long.parseLong(cursor.getString(8));
                }

                projects.add(new Project(Long.parseLong(cursor.getString(0)), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6), cursor.getString(7), myEventId));
            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return projects;
    }


}




