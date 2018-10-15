package com.example.elitepc.roomwordsample;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class WordViewModel extends AndroidViewModel {
    private WordRepository mRepository;
    private LiveData<List<Word>> mAllWords;
    private LiveData<List<Category>> mAllCategories;

    public WordViewModel (Application application){
        super(application);
        mRepository = new WordRepository(application);
        mAllWords = mRepository.getAllWords();
        mAllCategories = mRepository.getmAllCategories();
    }

    LiveData<List<Word>> getAllWords() {
        return mAllWords;
    }

    LiveData<List<Category>> getAllCategories() {
        return mAllCategories;
    }

    public void insert(Word word) {
        mRepository.insert(word);
    }

    public void insert(Oppgave oppgave) { mRepository.insert(oppgave);}
}
