package com.annabohm.pracainzynierska;

import java.util.Date;
import java.util.List;

public class Event {

    private String eventName;
    private Date eventDateStart;
    private Date eventTimeStart;
    private Date eventDateFinish;
    private Date eventTimeFinish;
    private String eventLocation;
    private String eventDescription;
    private User eventAuthor;
    private List<Invitation> eventInvitations;

    public Event() {

    }

    public Event(String eventName, Date eventDateStart, Date eventTimeStart, Date eventDateFinish, Date eventTimeFinish, String eventLocation, String eventDescription) {
        this.eventName = eventName;
        this.eventDateStart = eventDateStart;
        this.eventTimeStart = eventTimeStart;
        this.eventDateFinish = eventDateFinish;
        this.eventTimeFinish = eventTimeFinish;
        this.eventLocation = eventLocation;
        this.eventDescription = eventDescription;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventDateStart() {
        return eventDateStart;
    }

    public void setEventDateStart(Date eventDateStart) {
        this.eventDateStart = eventDateStart;
    }

    public Date getEventTimeStart() {
        return eventTimeStart;
    }

    public void setEventTimeStart(Date eventTimeStart) {
        this.eventTimeStart = eventTimeStart;
    }

    public Date getEventDateFinish() {
        return eventDateFinish;
    }

    public void setEventDateFinish(Date eventDateFinish) {
        this.eventDateFinish = eventDateFinish;
    }

    public Date getEventTimeFinish() {
        return eventTimeFinish;
    }

    public void setEventTimeFinish(Date eventTimeFinish) {
        this.eventTimeFinish = eventTimeFinish;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
}
