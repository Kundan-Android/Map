package com.kundankishore.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    Pickup_Api pickup_api;
    private GoogleMap googleMap;
    private double longitude;
    private double latitude;
    private GoogleApiClient googleApiClient;
    Example example;
    private EditText pickup_address, destn_address;
    public static final int RequestPermissionCode = 1;
    Marker marker_pick;
    private boolean isMapReady = false;
  //  PlacesAutocompleteTextView placeAutocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Initializing googleapi client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        pickup_address = (EditText) findViewById(R.id.pickup);
        destn_address = (EditText) findViewById(R.id.destination);
        destn_address.requestFocus();
      //  placeAutocomplete = findViewById(R.id.places_autocomplete);
       /* placeAutocomplete.setOnPlaceSelectedListener(
                new OnPlaceSelectedListener() {
                    @Override
                    public void onPlaceSelected(final Place place) {
                        // do something awesome with the selected place
                    }
                }
        );*/
        destn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (destn_address.hasFocus()) {
                    Intent intent = new Intent(MapsActivity.this, PlaceSelector.class);
                    intent.putExtra("type", "pickup");
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        if (!checkPermission()){
            //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            requestPermission();
        }

       // googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        googleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        isMapReady = true;
    }

    private void getCurrentLocation() {
        googleMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ABC","No Permission");
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            //moving the map to location
            moveMap();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        {
            String name;
            Double latitude,longitude;
            name=intent.getStringExtra("place");
            latitude=intent.getDoubleExtra("lat",0);
            longitude=intent.getDoubleExtra("lng",0);

            if (intent.getStringExtra("case").equals("1")) {
                Log.d("ABC","name: "+name);
                destn_address.setText(name);
                final LatLng latLng = new LatLng(latitude, longitude);
                Geocoder geocoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    final String str = addressList.get(0).getAddressLine(0);
                    /*MarkerOptions options=new MarkerOptions()
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_location),80,80,false)))
                            .position(new LatLng(latitude,longitude))
                            .snippet("Destination");
                    marker_pick= googleMap.addMarker(options);*/
                    if (googleMap != null && googleApiClient != null)
                    Log.d("ABC","adding Marker on destinations."+latLng);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //  drawMarker(latLng);
                            if (isMapReady) {
                                googleMap.clear();
                                googleMap.addMarker(new MarkerOptions()
                                        .position(latLng) //setting position
                                        .title(str)
                                        .draggable(true));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                //Animating the camera
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            else {
                Log.d("ABC","name: "+name);

            }
        }
    }
    private void drawMarker(LatLng point){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting latitude and longitude for the marker
        markerOptions.position(point);

        // Adding marker on the Google Map
        googleMap.addMarker(markerOptions);
    }
    private void goToLocationZoom(double v, double v1, float zoom) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        googleMap.moveCamera(update);
    }

    private void moveMap() {
      //  String msg = latitude + ", " + longitude;
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            String str = addressList.get(0).getAddressLine(0);
            /*googleMap.addMarker(new MarkerOptions()
                    .position(latLng) //setting position
                    .title(str)
                    .draggable(true));*/
            drawMarker(latLng);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String locality = getCompleteAddressString(latitude,longitude);
        pickup_address.setText(locality);

        //Moving the camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //Displaying current coordinates in toast
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        postData();

    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strAdd;
    }

    private void postData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Pickup_Api.Base_Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        pickup_api = retrofit.create(Pickup_Api.class);
        example = new Example(latitude,longitude);

        Call<Example> call = pickup_api.insertdata(example);


        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                Example data = response.body();
                Toast.makeText(MapsActivity.this, "success"+data, Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Failure", Toast.LENGTH_SHORT).show();


            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }
    public boolean checkPermission() {

        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        return SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult ==PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION

                }, RequestPermissionCode);

    }


}





