package com.example.elitepc.roomwordsample;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

@Entity(tableName = "task_child_join",
        primaryKeys = {"task_id", "child_task_id"},
        foreignKeys = {
                @ForeignKey(entity = Oppgave.class,
                        parentColumns = "id",
                        childColumns = "task_id"),
                @ForeignKey(entity = Oppgave.class,
                        parentColumns = "id",
                        childColumns = "child_task_id")
        })
public class task_child_join {
    public final int task_id;
    public final int child_task_id;

    public task_child_join(final int task_id, final int child_task_id){
        this.task_id = task_id;
        this.child_task_id = child_task_id;
    }
}
