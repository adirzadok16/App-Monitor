package com.example.appmonitor.Utilities;

public class appInformation {
    private String appName;
    private String time;
    private int hours;
    private int minutes;
    private String appIconBase64;
    private int timeUse;
    private String packageName;
    private boolean notification75;
    private boolean notification90;

    // Default constructor required for calls to DataSnapshot.getValue(appInformation.class)
    public appInformation() {
    }

    // Constructor
    public appInformation(String appName, String time, int hours, int minutes, String appIconBase64, String packageName) {
        this.appName = appName;
        this.time = time;
        this.hours = hours;
        this.minutes = minutes;
        this.appIconBase64 = appIconBase64;
        this.timeUse = 0;
        this.packageName = packageName;
        this.notification75 = false;
        this.notification90 = false;
    }

    // Getters and setters
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getAppIconBase64() {
        return appIconBase64;
    }

    public void setAppIconBase64(String appIconBase64) {
        this.appIconBase64 = appIconBase64;
    }

    public int getTimeInSec() {
        return this.hours * 3600 + this.minutes * 60;
    }

    public int getTimeUse() {
        return timeUse;
    }

    public void setTimeUse(int timeUse) {
        this.timeUse = timeUse;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean getNotification75(){
        return notification75;
    }

    public boolean getNotification90(){
        return notification90;
    }
}
