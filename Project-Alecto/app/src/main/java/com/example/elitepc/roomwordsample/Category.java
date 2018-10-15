package com.example.elitepc.roomwordsample;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/*Kategori skal bindes opp mot tasker i SQL
* Her skal kategorien brukes for å lage standard tasks innenfor kategorien
* Den skal også kunne koble seg opp mot både ukategoriserte og kategoriserte tasks
* Den skal kunne gjøre rede for hvor mange ganger samt hvor lang tid man har brukt totalt på oppgaver innenfor kategorien.
* */

@Entity(tableName = "category_table")
public class Category {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    Integer id = 0;


    private String cname;
    private String cdescription;

    public int getTotal_time() {
        return total_time;
    }

    public void setTotal_time(int total_time) {
        this.total_time = total_time;
    }

    private int total_time = 0;
    public Category(){}
    Category(String name){
        this.cname = name;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getCdescription() {
        return cdescription;
    }

    public void setCdescription(String cdescription) {
        this.cdescription = cdescription;
    }





}
