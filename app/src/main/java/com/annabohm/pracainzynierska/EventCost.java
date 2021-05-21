package com.annabohm.pracainzynierska;

public class EventCost {
    private String eventCostId;
    private String eventCostTitle;
    private int eventCostValue;
    private boolean eventCostPaid;

    public EventCost() {

    }

    public EventCost(String eventCostId, String eventCostTitle, int eventCostValue, boolean eventCostPaid) {
        this.eventCostId = eventCostId;
        this.eventCostTitle = eventCostTitle;
        this.eventCostValue = eventCostValue;
        this.eventCostPaid = eventCostPaid;
    }

    public String getEventCostId() {
        return eventCostId;
    }

    public void setEventCostId(String eventCostId) {
        this.eventCostId = eventCostId;
    }

    public String getEventCostTitle() {
        return eventCostTitle;
    }

    public void setEventCostTitle(String eventCostTitle) {
        this.eventCostTitle = eventCostTitle;
    }

    public int getEventCostValue() {
        return eventCostValue;
    }

    public void setEventCostValue(int eventCostValue) {
        this.eventCostValue = eventCostValue;
    }

    public boolean isEventCostPaid() {
        return eventCostPaid;
    }

    public void setEventCostPaid(boolean eventCostPaid) {
        this.eventCostPaid = eventCostPaid;
    }
}
