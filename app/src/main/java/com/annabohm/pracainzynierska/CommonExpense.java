package com.annabohm.pracainzynierska;

public class CommonExpense {
    private String CommonExpenseId;
    private String commonExpenseTitle;
    private String commonExpensePayingUserId;
    private  int commonExpenseValue;
    private boolean commonExpenseToSettle;

    public CommonExpense(){

    }

    public CommonExpense(String CommonExpenseId, String commonExpenseTitle, String commonExpensePayingUserId, int commonExpenseValue, boolean commonExpenseToSettle) {
        this.commonExpenseTitle = commonExpenseTitle;
        this.commonExpensePayingUserId = commonExpensePayingUserId;
        this.commonExpenseValue = commonExpenseValue;
        this.commonExpenseToSettle = commonExpenseToSettle;
    }

    public String getCommonExpenseId() {
        return CommonExpenseId;
    }

    public void setCommonExpenseId(String commonExpenseId) {
        CommonExpenseId = commonExpenseId;
    }

    public String getCommonExpenseTitle() {
        return commonExpenseTitle;
    }

    public void setCommonExpenseTitle(String commonExpenseTitle) {
        this.commonExpenseTitle = commonExpenseTitle;
    }

    public String getCommonExpensePayingUserId() {
        return commonExpensePayingUserId;
    }

    public void setCommonExpensePayingUserId(String commonExpensePayingUserId) {
        this.commonExpensePayingUserId = commonExpensePayingUserId;
    }

    public int getCommonExpenseValue() {
        return commonExpenseValue;
    }

    public void setCommonExpenseValue(int commonExpenseValue) {
        this.commonExpenseValue = commonExpenseValue;
    }

    public boolean isCommonExpenseToSettle() {
        return commonExpenseToSettle;
    }

    public void setCommonExpenseToSettle(boolean commonExpenseToSettle) {
        this.commonExpenseToSettle = commonExpenseToSettle;
    }
}
