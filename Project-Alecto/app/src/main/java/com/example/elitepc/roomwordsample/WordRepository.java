package com.example.elitepc.roomwordsample;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class WordRepository {
    private WordDao mWordDao;
    private LiveData<List<Word>> mAllWords;
    private LiveData<List<Category>> mAllCategories;

    WordRepository(Application application) {
        WordRoomDatabase db = WordRoomDatabase.getDatabase(application);
        mWordDao = db.wordDao();
        mAllWords = mWordDao.getAllWords();
        mAllCategories = mWordDao.getAllCategories();
    }

    LiveData<List<Word>> getAllWords() {
        return mAllWords;
    }
    LiveData<List<Category>> getmAllCategories() {
        return mAllCategories;
    }

    public void insert (Word word) {//insert skjer på en annen tråd enn main, derfor trenger man klassen under.
        new insertAsyncTask(mWordDao).execute(word);
    }

    public void insert (Oppgave oppgave) {//insert skjer på en annen tråd enn main, derfor trenger man klassen under.
        new insertAsyncTaskOppgave(mWordDao).execute(oppgave);
    }

    private static class insertAsyncTask extends AsyncTask<Word, Void, Void> {

        private WordDao mAsyncTaskDao;

        insertAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Word... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }

    }
    private static class insertAsyncTaskOppgave extends AsyncTask<Oppgave, Void, Void> {

        private WordDao mAsyncTaskDao;

        insertAsyncTaskOppgave(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Oppgave... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }

    }
}
