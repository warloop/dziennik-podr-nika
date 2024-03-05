package com.example.projekt_mobilne;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditEntryActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private RestCountriesAPI api;
    private EditText editTitle;
    private EditText editText;
    private TextView countryText;
    private Spinner countrySpinner;
    private ArrayAdapter<Country> countryArrayAdapter;
    private String selectedCountry;
    private TextView locationTextView;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final String EXTRA_EDIT_ENTRY_NAME = "com.example.EDIT_ENTRY_NAME";
    public static final String EXTRA_EDIT_ENTRY_TEXT = "com.example.EDIT_ENTRY_TEXT";
    public static final String EXTRA_EDIT_ENTRY_COUNTRY = "com.example.EDIT_ENTRY_COUNTRY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_entry);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        if(ContextCompat.checkSelfPermission(EditEntryActivity.this,Manifest.permission.POST_NOTIFICATIONS)!=(PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(EditEntryActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
        }
        }
        initializeRetrofit();
        setupViews();
        setupCountrySpinner();
        handleIntent();
        setupLocationManager();
    }

    private void initializeRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://restcountries.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(RestCountriesAPI.class);
    }

    private void setupViews() {
        editTitle= findViewById(R.id.edit_entry_name);
        editText = findViewById(R.id.edit_entry_text);
        countryText = findViewById(R.id.countryText);
        countrySpinner = findViewById(R.id.country_spinner);
        locationTextView = findViewById(R.id.locationText);
        imageView = findViewById(R.id.image_view);

        Button saveButton = findViewById(R.id.button_save);
        Button cameraButton = findViewById(R.id.button_camera);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry();
            }
        });

        Button launchButton = findViewById(R.id.launch);

        // Dodaj obsługę kliknięcia przycisku
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Wywołaj metodę do otwarcia Google Maps
                openGoogleMaps();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditEntryActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditEntryActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });
    }
    public void makeNotification(){
        String channelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        builder.setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(getString(R.string.notify))
        .setContentText(getString(R.string.notify_ok))
        .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent intent = new Intent(getApplicationContext(), Notification.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data","some value");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH; // Set the importance level
                notificationChannel = new NotificationChannel(channelID, "Some description", importance);

                // Configure the notification channel.
                notificationChannel.setLightColor(Color.GREEN); // Set the blink color for notification LED
                notificationChannel.enableVibration(true); // Set whether vibration is enabled for this channel.

                // Submit the notification channel object to the notification manager
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        notificationManager.notify(0, builder.build());

    }
    public String getSelectedCountry(){
        return selectedCountry;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private void setupCountrySpinner() {
        countryArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<Country>());
        countryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryArrayAdapter);

        api.getCountries().enqueue(new Callback<List<Country>>() {
            @Override
            public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    countryArrayAdapter.clear();
                    countryArrayAdapter.addAll(response.body());
                    countryArrayAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EditEntryActivity.this, "Failed to load countries", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                Toast.makeText(EditEntryActivity.this, "Error loading countries", Toast.LENGTH_SHORT).show();
            }
        });

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = countryArrayAdapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void handleIntent() {
        Intent starter = getIntent();
        if (starter.hasExtra(EXTRA_EDIT_ENTRY_NAME))
            editTitle.setText(starter.getStringExtra(EXTRA_EDIT_ENTRY_NAME));
        if (starter.hasExtra(EXTRA_EDIT_ENTRY_TEXT))
            editText.setText(starter.getStringExtra(EXTRA_EDIT_ENTRY_TEXT));
        if(starter.hasExtra(EXTRA_EDIT_ENTRY_COUNTRY))
            countryText.setText(starter.getStringExtra(EXTRA_EDIT_ENTRY_COUNTRY));
    }

    private void setCountryInSpinner(String countryName) {
        for (int i = 0; i < countryArrayAdapter.getCount(); i++) {
            if (countryArrayAdapter.getItem(i).getName().equals(countryName)) {
                countrySpinner.setSelection(i);
                break;
            }
        }
    }

    private void saveEntry() {
        Intent replyIntent = new Intent();
        if (TextUtils.isEmpty(editTitle.getText()) || TextUtils.isEmpty(editText.getText())) {
            setResult(RESULT_CANCELED, replyIntent);
            Toast.makeText(this, (getString(R.string.empty_not_saved)), Toast.LENGTH_LONG).show();
        } else {
            String name = editTitle.getText().toString();
            String text = editText.getText().toString();
            replyIntent.putExtra(EXTRA_EDIT_ENTRY_NAME, name);
            replyIntent.putExtra(EXTRA_EDIT_ENTRY_TEXT, text);
            replyIntent.putExtra(EXTRA_EDIT_ENTRY_COUNTRY, selectedCountry);

            setResult(RESULT_OK, replyIntent);
            makeNotification(); // Wywołanie tworzenia powiadomienia
            finish();
        }
    }


    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationTextView.setText("Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(EditEntryActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void openGoogleMaps() {
        // Sprawdź czy Google Maps jest zainstalowane
        if (isGoogleMapsInstalled()) {
            // Tworzymy URI dla Google Maps z docelową lokalizacją (możesz dostosować współrzędne)
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=latitude,longitude");

            // Tworzymy intencję
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

            // Ustawiamy pakiet na Google Maps
            mapIntent.setPackage("com.google.android.apps.maps");

            // Uruchamiamy intencję
            startActivity(mapIntent);
        }
    }

    // Metoda sprawdzająca, czy Google Maps jest zainstalowane
    private boolean isGoogleMapsInstalled() {
        try {
            // Sprawdzamy czy jest zainstalowana aplikacja o nazwie "com.google.android.apps.maps"
            getPackageManager().getPackageInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
