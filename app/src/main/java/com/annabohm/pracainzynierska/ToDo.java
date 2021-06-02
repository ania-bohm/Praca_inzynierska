package com.annabohm.pracainzynierska;

public class ToDo {
    private String toDoId;
    private String toDoTitle;
    private String toDoDescription;
    private String toDoCreatorId;
    private boolean toDoChecked;

    public ToDo() {
    }

    public ToDo(String toDoId, String toDoTitle, String toDoDescription, String toDoCreatorId, boolean toDoChecked) {
        this.toDoId = toDoId;
        this.toDoTitle = toDoTitle;
        this.toDoDescription = toDoDescription;
        this.toDoCreatorId = toDoCreatorId;
        this.toDoChecked = toDoChecked;
    }

    public String getToDoId() {
        return toDoId;
    }

    public void setToDoId(String toDoId) {
        this.toDoId = toDoId;
    }

    public String getToDoTitle() {
        return toDoTitle;
    }

    public void setToDoTitle(String toDoTitle) {
        this.toDoTitle = toDoTitle;
    }

    public String getToDoDescription() {
        return toDoDescription;
    }

    public void setToDoDescription(String toDoDescription) {
        this.toDoDescription = toDoDescription;
    }

    public String getToDoCreatorId() {
        return toDoCreatorId;
    }

    public void setToDoCreatorId(String toDoCreatorId) {
        this.toDoCreatorId = toDoCreatorId;
    }

    public boolean isToDoChecked() {
        return toDoChecked;
    }

    public void setToDoChecked(boolean toDoChecked) {
        this.toDoChecked = toDoChecked;
    }
}
