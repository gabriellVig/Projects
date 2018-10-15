package com.example.elitepc.roomwordsample;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "tasks_table")
public class Oppgave {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    Integer id = 0;

    private String tname;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTname() {
        return tname;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }

    public String getTdescription() {
        return tdescription;
    }

    public void setTdescription(String tdescription) {
        this.tdescription = tdescription;
    }

    public String getTgoal() {
        return tgoal;
    }

    public void setTgoal(String tgoal) {
        this.tgoal = tgoal;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getTask_chosen() {
        return task_chosen;
    }

    public void setTask_chosen(int task_chosen) {
        this.task_chosen = task_chosen;
    }

    public int getTotal_time() {
        return total_time;
    }

    public void setTotal_time(int total_time) {
        this.total_time = total_time;
    }

    public boolean isInterval() {
        return interval;
    }

    public void setInterval(boolean interval) {
        this.interval = interval;
    }

    public int getInterval_days() {
        return interval_days;
    }

    public void setInterval_days(int interval_days) {
        this.interval_days = interval_days;
    }

    private String tdescription;
    private String tgoal;

    private int timer;

    private int task_chosen = 0;
    private int total_time = 0;

    private boolean interval;
    private int interval_days;

}
