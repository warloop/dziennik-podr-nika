package com.example.projekt_mobilne;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Entry.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class EntryDatabase extends RoomDatabase {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    byte[] image = Converters.fromBitmap(bitmap);

    public abstract EntryDao entryDao();
    private static volatile EntryDatabase INSTANCE;
    public static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static EntryDatabase getDatabase(final Context context) {
        if (INSTANCE == null)
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), EntryDatabase.class, "entry_db4")
                    .addCallback(sRoomDatabaseCallback)
                    .build();
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(() -> {
                EntryDao dao = INSTANCE.entryDao();
                dao.deleteAll();

                Entry entry = new Entry("Wpis1", "podróż była fajna", "Poland", new Date(), "d");
                dao.insert(entry);
                entry = new Entry("Wpis1", "podróż była fajna", "Poland", new Date(), "d");
                dao.insert(entry);
                entry = new Entry("Wpis1", "podróż była fajna", "Poland", new Date(), "d");
                dao.insert(entry);
                entry = new Entry("Wpis2", "podróż była fajna", "Poland", new Date(), "d");
                dao.insert(entry);
                entry = new Entry("Wpis2", "podróż była fajna", "Poland", new Date(), "d");
                dao.insert(entry);
            });
        }
    };
}
