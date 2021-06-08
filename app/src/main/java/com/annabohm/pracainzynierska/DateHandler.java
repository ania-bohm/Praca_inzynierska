package com.annabohm.pracainzynierska;

import com.google.android.material.datepicker.CalendarConstraints;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHandler {
    private String dateFormat;
    private String timeFormat;

    public DateHandler(String dateFormat, String timeFormat) {
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
    }

    public boolean isDateValid(String dateStr) {
        DateFormat sdf = new SimpleDateFormat(this.dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public boolean isTimeValid(String dateStr) {
        DateFormat sdf = new SimpleDateFormat(this.timeFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public Date convertStringToDate(String dateString) {
        DateFormat dateFormatter = new SimpleDateFormat(this.dateFormat);
        Date date = null;
        try {
            date = dateFormatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertDateToString(Date date) {
        DateFormat dateFormatterPrint = new SimpleDateFormat(this.dateFormat);
        return dateFormatterPrint.format(date);
    }

    public Date convertStringToTime(String timeString) {
        DateFormat timeFormatter = new SimpleDateFormat(this.timeFormat);
        Date time = null;
        try {
            time = timeFormatter.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public String convertTimeToString(Date time) {
        DateFormat timeFormatterPrint = new SimpleDateFormat(this.timeFormat);
        return timeFormatterPrint.format(time);
    }

    public boolean datesCorrect(Date dateStart, Date dateFinish, Date timeStart, Date timeFinish) {
        Date dateStartMerged = mergeDateAndTime(dateStart, timeStart);
        Date dateFinishMerged = mergeDateAndTime(dateFinish, timeFinish);
        return !dateStartMerged.after(dateFinishMerged);
    }

    public Date mergeDateAndTime(Date date, Date time) {
        Calendar calendarA = Calendar.getInstance();
        calendarA.setTime(date);

        Calendar calendarB = Calendar.getInstance();
        calendarB.setTime(time);

        calendarA.set(Calendar.HOUR_OF_DAY, calendarB.get(Calendar.HOUR_OF_DAY));
        calendarA.set(Calendar.MINUTE, calendarB.get(Calendar.MINUTE));
        calendarA.set(Calendar.SECOND, calendarB.get(Calendar.SECOND));
        calendarA.set(Calendar.MILLISECOND, calendarB.get(Calendar.MILLISECOND));

        return calendarA.getTime();
    }

}
