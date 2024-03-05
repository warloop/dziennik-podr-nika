package com.example.projekt_mobilne;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Entry entry);

    @Update
    void update(Entry entry);

    @Delete
    void delete(Entry entry);

    @Query("DELETE FROM entries")
    void deleteAll();

    @Query("SELECT * FROM entries ORDER BY date")
    LiveData<List<Entry>>findAll();
}
