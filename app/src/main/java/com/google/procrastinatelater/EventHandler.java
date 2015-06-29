package com.google.procrastinatelater;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Nicole on 29-Jun-15.
 */
public class EventHandler {
    Context iAppContext;

    public EventHandler(Context anApplicationContext) {
        iAppContext = anApplicationContext;
    }

    public Intent createEvent(Project aProject){
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

            intent.putExtra("endTime", cal.getTimeInMillis()+calculateSessionLength(strHrs, strMins));
            intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq));


        }else if (!b_cmt && b_due && b_howLong && b_frq){ //we know session frequency and length, and when the project is due

            intent.putExtra("endTime", cal.getTimeInMillis()+calculateSessionLength(strHrs, strMins));
            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            String untilDate = parseEndDate(stringToCalendar(strDue));
            if (untilDate != null){
                //Toast.makeText(iAppContext, "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq));
            }


        }else if (b_cmt && !b_due && b_howLong && b_frq){ //we know time commitment, and session length and frequency

            float sessionInMs = calculateSessionLength(strHrs, strMins);
            float projectCmt = Float.parseFloat(strCmt);

            intent.putExtra("endTime", cal.getTimeInMillis() + sessionInMs); //not using float sessionInMs because of calendar bug
            intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq) + ";COUNT=" + calculateSessionCount(projectCmt, sessionInMs));


        }else if (b_cmt && b_due && !b_howLong && b_frq){   //we know time commitment, the due date, and session frequency

            Calendar endCalendar = stringToCalendar(strDue);

            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            String untilDate = parseEndDate(endCalendar);
            if (untilDate != null){
                //Toast.makeText(iAppContext, "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq));
            }

            //put session length
            intent.putExtra("endTime", cal.getTimeInMillis() + calculateSessionLength(strCmt, strFrq, cal, endCalendar));


        }else if (b_cmt && b_due && b_howLong && !b_frq){ //we know time commitment, the due date, and session length

            int sessionInMs = calculateSessionLength(strHrs, strMins);
            Calendar endCalendar = stringToCalendar(strDue);
            String untilDate = parseEndDate(endCalendar);
            String sessionFrq = calculateSessionFrq(strCmt, sessionInMs, cal, endCalendar);

            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            if (untilDate != null){
                //Toast.makeText(iAppContext, "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(sessionFrq) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(sessionFrq));
            }

            //put session length
            intent.putExtra("endTime", cal.getTimeInMillis()+sessionInMs);


        }else if (b_cmt && b_due && b_howLong && b_frq){

            //fill in due date, session length, and session frequency

            int sessionInMs = calculateSessionLength(strHrs, strMins);
            float projectCmt = Float.parseFloat(strCmt);
            Calendar endCalendar = stringToCalendar(strDue);
            String untilDate = parseEndDate(endCalendar);

            intent.putExtra("endTime", cal.getTimeInMillis() + calculateSessionLength(strHrs, strMins)); //not using float sessionInMs because of calendar bug

            //if we can read the due date, repeat at frequency desired until then. Otherwise, repeat forever.
            if (untilDate != null){
                //Toast.makeText(iAppContext, "Until " + untilDate, Toast.LENGTH_SHORT).show();
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq) + ";COUNT=" + calculateSessionCount(projectCmt, sessionInMs) + ";UNTIL=" + untilDate);
            }else{
                //a note will be 'Toasted' through putEndDate if we couldn't read the end date
                intent.putExtra("rrule", "FREQ=" + parseSessionFrequency(strFrq)+ ";COUNT=" + calculateSessionCount(projectCmt, sessionInMs));
            }

            //check due date.
            int tentativeFrq = Integer.parseInt(calculateSessionFrq(strCmt, sessionInMs, cal, endCalendar));
            if (tentativeFrq > Integer.parseInt(strFrq)){
                Toast.makeText(iAppContext, "Warning: Given these criteria, you will not finish this project on time.", Toast.LENGTH_LONG).show();
            }

        }

        return intent;
    }


    private String calculateSessionFrq(String aCmt, int aSessionInMs, Calendar aStart, Calendar anEnd){
        String projectFrq;
        float projectCmt = Float.parseFloat(aCmt);
        float sessionInHrs = (float)aSessionInMs/(1000*60*60);
        //Toast.makeText(iAppContext, "Length in Hrs: " + sessionInHrs, Toast.LENGTH_SHORT).show();
        int numSessions = (int)Math.ceil(projectCmt/sessionInHrs);
        //Toast.makeText(iAppContext, "Number of Session Needed: " + numSessions, Toast.LENGTH_SHORT).show();


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
            Toast.makeText(iAppContext, "Warning: You will not finish this project on time without increasing your session length." , Toast.LENGTH_LONG).show();
        }

        return projectFrq;
    }

    private int calculateSessionLength(String aCmt, String aFrq, Calendar aStart, Calendar anEnd){
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
        //Toast.makeText(iAppContext, "numSessions" + numSessions, Toast.LENGTH_SHORT).show();

        //step two: divide cmt into number of sessions to find time per session
        Float sessionLengthInHrs = (Float.parseFloat(aCmt)/numSessions);
        //Toast.makeText(iAppContext, "Session Length: " + sessionLengthInHrs, Toast.LENGTH_SHORT).show();
        int sessionLengthInMs = (int)(1000*60*60*sessionLengthInHrs);

        return sessionLengthInMs;
    }

    /**
     *
     * @param aCmt time needed to complete project; its time commitment
     * @param aSessionInMs length of sessions IN MILLISECONDS
     * @return
     */
    private int calculateSessionCount(float aCmt, float aSessionInMs){
        float sessionInHrs = aSessionInMs/(1000*60*60); //calculate session in hours
        int numSessions = (int)Math.ceil(aCmt/sessionInHrs); //total time divided by session length = number of session needed
        //Toast.makeText(iAppContext, "" + aCmt + "/" + sessionInHrs + "= " + (hrsTotal/sessionInHrs) , Toast.LENGTH_SHORT).show();
        //Toast.makeText(iAppContext, "numSessions" + numSessions, Toast.LENGTH_SHORT).show();
        return numSessions;
    }

    private Calendar stringToCalendar(String aDateString){
        Calendar myCal = Calendar.getInstance();
        SimpleDateFormat formatIn = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

        try {

            Date projectDate = formatIn.parse(aDateString.toLowerCase()); //use format to parse parameter into Date projectDate
            myCal.setTime(projectDate);
        } catch (ParseException e) {
            Toast.makeText(iAppContext, "We were unable use your due date", Toast.LENGTH_SHORT).show();
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "could not parse due date");
            //e.printStackTrace();
            return null;
        }

        return myCal;
    }

    private String parseEndDate(Calendar anEndCal){

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


    private int calculateSessionLength(String aStrHrs, String aStrMins){
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

    private String parseSessionFrequency(String aStrFrq){
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
