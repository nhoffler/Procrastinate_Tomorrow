package com.google.procrastinatelater;

import android.net.Uri;

import java.net.URI;

/**
 * Created by Nicole on 06-May-15.
 */
public class Project {
    private long iId;
    private String iName, iCmt, iDueDate, iSnHrs, iSnMins, iSnFrq, iImgPath;

    public Project(long aId, String aName, String aCmt, String aDueDate, String aSnHrs, String aSnMins, String aSnFrq, String aImgPath) {
        iId = aId;
        iName = aName;
        iCmt = aCmt;
        iDueDate = aDueDate;
        iSnHrs = aSnHrs;
        iSnMins = aSnMins;
        iSnFrq = aSnFrq;
        iImgPath = aImgPath;
    }

    public long getId() {
        return iId;
    }

    public void setId(long aId){
        iId = aId;
    }


    public String getName() {
        return iName;
    }

    public void setName(String aName) {
        iName = aName;
    }

    public String getCmt() {
        return iCmt;
    }

    public void setCmt(String aCmt) {
        iCmt = aCmt;
    }

    public String getDueDate() {
        return iDueDate;
    }

    public void setDueDate(String aDueDate) {
        iDueDate = aDueDate;
    }

    public String getSnHrs() {
        return iSnHrs;
    }

    public void setSnHrs(String aSnHrs) {
        iSnHrs = aSnHrs;
    }

    public String getSnMins() {
        return iSnMins;
    }

    public void setSnMins(String aSnMins) {
        iSnMins = aSnMins;
    }

    public String getSnFrq() {
        return iSnFrq;
    }

    public void setSnFrq(String aSnFrq) {
        iSnFrq = aSnFrq;
    }

    public String getImgPath() {
        return iImgPath;
    }

    public void setImgPath(String aImgPath) {
        iImgPath = aImgPath;
    }
}
