package com.annabohm.pracainzynierska;

public class Score {
    private String scoreId;
    private String scoreGuestName;
    private int scoreValue;

    public Score() {
    }

    public Score(String scoreId, String scoreGuestName, int scoreValue) {
        this.scoreId = scoreId;
        this.scoreGuestName = scoreGuestName;
        this.scoreValue = scoreValue;
    }

    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId;
    }

    public String getScoreGuestName() {
        return scoreGuestName;
    }

    public void setScoreGuestName(String scoreGuestName) {
        this.scoreGuestName = scoreGuestName;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }
}
