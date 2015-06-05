package com.google.procrastinatelater;

import android.net.Uri;

import java.net.URI;

/**
 * Created by Nicole on 06-May-15.
 */
public class Project {
    private String _name, _cmt, _dueDate, _snHrs, _snMins, _snFrq, _imgPath;
    private int _id;

    public Project (int id, String projectName, String timeCmt, String dueDate, String sessionHrs, String sessionMins, String sessionFrq, String imgPath){
        _id = id;
        _name = projectName;
        _cmt = timeCmt;
        _dueDate = dueDate;
        _snHrs = sessionHrs;
        _snMins = sessionMins;
        _snFrq = sessionFrq;
        _imgPath = imgPath;
    }

    public String getName() {
        return _name;
    }

    public String getCmt() {
        return _cmt;
    }

    public int getId() {return _id; }

    public String getDate() {
        return _dueDate;
    }

    public String getSnHrs() {
        return _snHrs;
    }

    public String getSnMins() {
        return _snMins;
    }

    public String getSnFrq() {
        return _snFrq;
    }

    public String getImgPath() { return _imgPath; }
}
