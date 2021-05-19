package com.annabohm.pracainzynierska;

public class ToDo {
    private String toDoTitle;
    private String toDoDescription;
    private boolean toDoChecked;

    public ToDo() {
    }

    public ToDo(String toDoTitle, String toDoDescription, boolean toDoChecked) {
        this.toDoTitle = toDoTitle;
        this.toDoDescription = toDoDescription;
        this.toDoChecked = toDoChecked;
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

    public boolean isToDoChecked() {
        return toDoChecked;
    }

    public void setToDoChecked(boolean toDoChecked) {
        this.toDoChecked = toDoChecked;
    }
}
