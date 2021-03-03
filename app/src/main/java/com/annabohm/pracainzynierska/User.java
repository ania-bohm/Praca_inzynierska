package com.annabohm.pracainzynierska;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userPhoneNumber;
    private String userPhoto;
    private List<Event> userEvents;
    private List<Invitation> userInvitations;

    public User() {

    }

    public User(String userFirstName, String userLastName, String userEmail, String userPhoneNumber) {
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
        userEvents = new ArrayList<>();
        userInvitations = new ArrayList<>();
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public List<Event> getUserEvents() {
        return userEvents;
    }

    public void setUserEvents(List<Event> userEvents) {
        this.userEvents = userEvents;
    }

    public List<Invitation> getUserInvitations() {
        return userInvitations;
    }

    public void setUserInvitations(List<Invitation> userInvitations) {
        this.userInvitations = userInvitations;
    }

}
