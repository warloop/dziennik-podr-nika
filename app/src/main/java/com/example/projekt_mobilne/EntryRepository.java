package com.example.projekt_mobilne;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class EntryRepository {
    private EntryDao entryDao;
    private LiveData<List<Entry>> entries;

    EntryRepository(Application application) {
        EntryDatabase database = EntryDatabase.getDatabase(application);
        entryDao = database.entryDao();
        entries = entryDao.findAll();
    }

    LiveData<List<Entry>> findAll() {
        return entries;
    }

    void insert(Entry entry) {
        EntryDatabase.databaseWriteExecutor.execute(() -> {
            entryDao.insert(entry);
        });
    }

    void update(Entry entry) {
        EntryDatabase.databaseWriteExecutor.execute(() -> {
            entryDao.update(entry);
        });
    }

    void delete(Entry entry) {
        EntryDatabase.databaseWriteExecutor.execute(() -> {
            entryDao.delete(entry);
        });
    }
}
