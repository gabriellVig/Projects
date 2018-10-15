package com.example.elitepc.roomwordsample;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;


/* TO do listen er i databasen en tabell med id for listen.
 * Listen inneholder flere oppgaver. Rekkefølgen på oppgavene er satt i tabellen ved index.
 * id for listen og index er primærnøkkel i databasen.
 * Fremmednøkkelen til en rad er altså en oppgave-id.
* */
@Entity(tableName = "to_do_lists",
        primaryKeys = {"list_ID", "index"},
        foreignKeys = {
                @ForeignKey(entity = Oppgave.class,
                        parentColumns = "id",
                        childColumns = "task_id")})
public class To_do_lists {
    //list_ID & index = PrimaryKey
    private int list_ID;//id for which list it is.
    private int index;//index for where in the list a task is.

    private void setindex(int index){
        this.index = index;
    }

    public To_do_lists(){

    }
    /*Need to change this so that it can store a category instead of a task
    * I could also create a new database which is called list_element
    * This list element could store either a task or a category.
    * I might also implement it so that a category & task could switch every other day in the list item.
    * Could also implement this without creating a new table.*/
    //task_id = ForeignKey
    private Integer task_id;//id for which task is on the list.
    private To_do_lists(final int list_ID, final int index, final Integer task_id){
        this.list_ID = list_ID;
        this.index = index;
        this.task_id = task_id;
    }

    public int getList_ID() {
        return list_ID;
    }

    public int getIndex() {
        return index;
    }

    public Integer getTask_id() {
        return task_id;
    }
}
