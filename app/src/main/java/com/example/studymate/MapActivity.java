package com.example.studymate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.*;

import java.io.IOException;
import java.util.*;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private AutoCompleteTextView searchInput;
    private Button searchBtn, viewLocationsBtn, backBtn;

    private Map<Marker, DocumentSnapshot> markerNoteMap = new HashMap<>();
    private static final int LOCATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI elements
        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
        viewLocationsBtn = findViewById(R.id.viewLocationsBtn);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());
        viewLocationsBtn.setOnClickListener(v -> loadAllNotePins());
        searchBtn.setOnClickListener(v -> searchLocation());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        enableUserLocation();
        loadAllNotePins();

        mMap.setOnMarkerClickListener(marker -> {
            DocumentSnapshot noteDoc = markerNoteMap.get(marker);
            if (noteDoc != null) showNoteBottomSheet(noteDoc);
            return true;
        });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        mMap.setMyLocationEnabled(true);

        LocationRequest request = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                fusedLocationClient.removeLocationUpdates(this);
                Location location = result.getLastLocation();
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
    }

    private void loadAllNotePins() {
        db.collection("study_notes").get().addOnSuccessListener(querySnapshot -> {
            mMap.clear();
            markerNoteMap.clear();

            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boolean hasPins = false;

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Double lat = doc.getDouble("latitude");
                Double lng = doc.getDouble("longitude");
                String title = doc.getString("title");

                if (lat != null && lng != null && title != null) {
                    LatLng position = new LatLng(lat, lng);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(title));
                    markerNoteMap.put(marker, doc);

                    boundsBuilder.include(position);
                    hasPins = true;
                }
            }

            if (hasPins) {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 120;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } else {
                Toast.makeText(this, "No locations found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoteBottomSheet(DocumentSnapshot doc) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_note_details, null);

        TextView titleView = view.findViewById(R.id.noteTitle);
        TextView dateView = view.findViewById(R.id.noteDate);
        TextView locationView = view.findViewById(R.id.noteLocation);
        ImageView noteImage = view.findViewById(R.id.noteImage);

        String title = doc.getString("title");
        String date = doc.getString("date");
        String imageBase64 = doc.getString("imagePath");
        String locationName = doc.getString("locationName");

        titleView.setText(title != null ? title : "No title");
        dateView.setText(date != null ? "📅 " + date : "📅 -");
        locationView.setText(locationName != null && !locationName.isEmpty()
                ? "📍 " + locationName
                : "📍 Location not available");

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            noteImage.setImageBitmap(bitmap);
        } else {
            noteImage.setImageResource(R.drawable.placeholder_image);
        }

        sheet.setContentView(view);
        sheet.show();
    }

    private void searchLocation() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 17));
                mMap.addMarker(new MarkerOptions()
                        .position(searchedLatLng)
                        .title("Searched: " + query));
            } else {
                Toast.makeText(this, "No results found for \"" + query + "\"", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error using geocoder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
