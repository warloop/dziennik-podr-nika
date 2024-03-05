package com.example.projekt_mobilne;

// Importowanie potrzebnych klas
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

// Główna klasa aktywności
public class MainActivity extends AppCompatActivity {
    private EntryViewModel entryViewModel; // ViewModel dla wpisów
    public static final int NEW_ENTRY_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ENTRY_ACTIVITY_REQUEST_CODE = 2;
    private Entry editedEntry = null; // Obiekt do przechowywania edytowanego wpisu
    private String lastSelectedCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ustawienie paska narzędzi (Toolbar) jako paska akcji
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle((getString(R.string.toolbar)));
        }

        // Konfiguracja RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final EntryAdapter adapter = new EntryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ustawienie obsługi przesuwania elementów w RecyclerView
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // Brak obsługi przenoszenia
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Usunięcie wpisu po przesunięciu
                int position = viewHolder.getAdapterPosition();
                Entry swipedEntry = adapter.getEntryAtPosition(position);
                if (swipedEntry != null) {
                    entryViewModel.delete(swipedEntry);
                    Snackbar.make(findViewById(R.id.coordinator_layout),
                                    getString(R.string.entry_archived),
                                    Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Inicjalizacja ViewModel
        entryViewModel = ViewModelProviders.of(this).get(EntryViewModel.class);
        entryViewModel.findAll().observe(this, new Observer<List<Entry>>() {
            @Override
            public void onChanged(@Nullable final List<Entry> entries) {
                adapter.setEntries(entries); // Aktualizacja danych w adapterze
            }
        });

        // Konfiguracja przycisku dodawania nowego wpisu
        FloatingActionButton addEntryButton = findViewById(R.id.add_button);
        addEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Uruchomienie aktywności do dodawania wpisu
                Intent intent = new Intent(MainActivity.this, EditEntryActivity.class);
                startActivityForResult(intent, NEW_ENTRY_ACTIVITY_REQUEST_CODE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Tworzenie menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obsługa kliknięć w menu
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Obsługa kliknięcia w ustawienia
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra(EditEntryActivity.EXTRA_EDIT_ENTRY_NAME);
            String text = data.getStringExtra(EditEntryActivity.EXTRA_EDIT_ENTRY_TEXT);
            String countryName = data.getStringExtra(EditEntryActivity.EXTRA_EDIT_ENTRY_COUNTRY); // Odczyt nazwy kraju
            lastSelectedCountry = countryName;
            if (requestCode == NEW_ENTRY_ACTIVITY_REQUEST_CODE) {
                // Dodanie nowego wpisu
                Entry entry = new Entry(name, text, countryName, new Date(), " ");
                entryViewModel.insert(entry);
                Snackbar.make(findViewById(R.id.coordinator_layout),
                                getString(R.string.entry_added),
                                Snackbar.LENGTH_LONG)
                        .show();
            } else if (requestCode == EDIT_ENTRY_ACTIVITY_REQUEST_CODE && editedEntry != null) {
                // Aktualizacja istniejącego wpisu
                editedEntry.setName(name);
                editedEntry.setText(text);
                editedEntry.setCountry(countryName);
                entryViewModel.update(editedEntry);
                editedEntry = null; // Resetowanie edytowanego wpisu
                Snackbar.make(findViewById(R.id.coordinator_layout),
                                getString(R.string.entry_edited),
                                Snackbar.LENGTH_LONG)
                        .show();
            }
        } else if (requestCode == EDIT_ENTRY_ACTIVITY_REQUEST_CODE && editedEntry != null) {


        } else {
            // Komunikat, jeśli użytkownik anulował akcję
            Snackbar.make(findViewById(R.id.coordinator_layout),
                            getString(R.string.empty_not_saved),
                            Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    // Klasa ViewHolder dla elementów listy
    private class EntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView entryTitleTextView;
        private TextView entryAuthorTextView;
        private Entry entry;

        public EntryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.entry_list_item, parent, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            entryTitleTextView = itemView.findViewById(R.id.entry_name);
            entryAuthorTextView = itemView.findViewById(R.id.entry_text);
        }

        public void bind(Entry entry) {
            // Przypisanie danych wpisu do widoków
            this.entry = entry;
            entryTitleTextView.setText(entry.getName());
            entryAuthorTextView.setText(entry.getText());
        }

        @Override
        public void onClick(View v) {
            // Obsługa kliknięcia w element listy
            MainActivity.this.editedEntry = this.entry;
            Intent intent = new Intent(MainActivity.this, EditEntryActivity.class);
            intent.putExtra(EditEntryActivity.EXTRA_EDIT_ENTRY_NAME, entry.getName());
            intent.putExtra(EditEntryActivity.EXTRA_EDIT_ENTRY_TEXT, entry.getText());
            intent.putExtra(EditEntryActivity.EXTRA_EDIT_ENTRY_COUNTRY, lastSelectedCountry); // Przekazywanie nazwy kraju
            startActivityForResult(intent, EDIT_ENTRY_ACTIVITY_REQUEST_CODE);
        }

        @Override
        public boolean onLongClick(View v) {
            // Usunięcie wpisu po długim kliknięciu
            MainActivity.this.entryViewModel.delete(this.entry);
            Snackbar.make(findViewById(R.id.coordinator_layout),
                            getString(R.string.entry_archived),
                            Snackbar.LENGTH_LONG)
                    .show();
            return true;
        }
    }

    // Adapter dla RecyclerView
    private class EntryAdapter extends RecyclerView.Adapter<EntryHolder> {
        private List<Entry> entries; // Lista wpisów

        @NonNull
        @Override
        public EntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Tworzenie nowego ViewHoldera
            return new EntryHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull EntryHolder holder, int position) {
            // Przypisanie danych wpisu do ViewHoldera
            if (entries != null) {
                Entry entry = entries.get(position);
                holder.bind(entry);
            }
            else {
                Log.d("MainActivity", "No entries");
            }
        }

        @Override
        public int getItemCount() {
            // Zwracanie liczby elementów w liście
            if (entries != null) {
                return entries.size();
            }
            return 0;
        }

        void setEntries(List<Entry> entries) {
            // Aktualizacja listy wpisów
            this.entries = entries;
            notifyDataSetChanged();
        }

        public Entry getEntryAtPosition(int position) {
            // Pobranie wpisu na danej pozycji
            if (entries != null && position >= 0 && position < entries.size()) {
                return entries.get(position);
            }
            return null;
        }
    }
}

