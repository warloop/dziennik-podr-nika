package com.example.projekt_mobilne;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class EntryViewModel extends AndroidViewModel {
    private EntryRepository entryRepository;
    private LiveData<List<Entry>> entries;

    public EntryViewModel(@NonNull Application application) {
        super(application);
        entryRepository = new EntryRepository(application);
        entries = entryRepository.findAll();
    }

    public LiveData<List<Entry>> findAll() {
        return entries;
    }

    public void insert(Entry entry) {
        entryRepository.insert(entry);
    }

    public void update(Entry entry) {
        entryRepository.update(entry);
    }

    public void delete(Entry entry) {
        entryRepository.delete(entry);
    }
}
