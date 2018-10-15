package com.example.elitepc.roomwordsample;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;


@Dao
public interface WordDao {

    @Insert
    void insert(Word word);

    @Insert
    void insert(Oppgave oppgave);

    @Query("DELETE FROM word_table")
    void deleteAll();

    @Query("SELECT * FROM WORD_TABLE ORDER BY WORD_TABLE.word ASC")
    LiveData<List<Word>> getAllWords();

    @Query("SELECT * FROM category_table ORDER BY category_table.id ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM tasks_table")
    List<Oppgave> getAllTasks();

    @Query("SELECT * FROM tasks_table " +
            "INNER JOIN task_category_join ON tasks_table.id=task_category_join.task_id " +
            "WHERE task_category_join.category_id=:category_id")
    List<Oppgave> getTaskForCategory(final int category_id);

    @Query("SELECT * FROM category_table " +
            "INNER JOIN task_category_join ON category_table.id=task_category_join.category_id " +
            "WHERE task_category_join.task_id=:task_id")
    List<Category> getCategoriesForTask(final int task_id);

    @Query("SELECT * FROM tasks_table " +
            "INNER JOIN task_child_join ON tasks_table.id = task_child_join.task_id " +
            "WHERE task_child_join.task_id=:task_id")
    List<Oppgave> getChildrenTasksForTask(final int task_id);

    @Query("SELECT * FROM tasks_table INNER JOIN task_child_join ON tasks_table.id = task_child_join.task_id WHERE task_child_join.child_task_id=:task_id")
    List<Oppgave> getParentTasksForTask(final int task_id);
}
