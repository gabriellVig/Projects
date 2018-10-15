package com.example.elitepc.roomwordsample;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
/*Tabell for å joine oppgaver med kategorier. M:N forhold.
* */

//  https://android.jlelse.eu/android-architecture-components-room-relationships-bf473510c14a
@Entity(tableName = "task_category_join",
        primaryKeys = {"task_id", "category_id"},
        foreignKeys = {
            @ForeignKey(entity = Oppgave.class,
                        parentColumns = "id",
                        childColumns = "task_id"),
            @ForeignKey(entity = Category.class,
                    parentColumns = "id",
                    childColumns = "category_id")
        })
//lag verdier kalt category_id og task_id
public class task_cat {
        public final int task_id;
        public final int category_id;

        public task_cat(final int task_id, final int category_id){
                this.task_id = task_id;
                this.category_id = category_id;
        }
}
