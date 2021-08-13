package com.example.myapplication.Services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

import javax.inject.Inject;

public class Start_Check_GeoPosition_Service {

    private final Activity activity;
    private final Context context;
    private String selectedUser;

    //todo: values to update user location
    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isLocationUpdateActive;


    public Start_Check_GeoPosition_Service(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public void setSelectedUser(String selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void updateLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);

        buildLocationRequest();

        buildLocationCallback();

        buildLocationSettingsRequest();

        startLocationUpdate();
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create().setInterval(10000).setFastestInterval(6000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                updateLocationUi();
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

    }

    public void startLocationUpdate() {

        if (checkPermForTakeLocation()) {
            isLocationUpdateActive = true;

            settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                            (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    updateLocationUi();
                }
            });
        }

    }

    public void stopLocationUpdates() {

        if (!isLocationUpdateActive) {
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isLocationUpdateActive = false;
            }
        });

    }

    private boolean checkPermForTakeLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, App_Constants.REQUESTCODE_LOCATION_FIRST);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, App_Constants.REQUESTCODE_LOCATION_LAST);
            return true;
        }
    }

    private void updateLocationUi() {
        if (currentLocation != null) {
            DatabaseReference userDataLocation = FirebaseDatabase.getInstance().getReference().child("Users_Locations");
            GeoFire geoFire = new GeoFire(userDataLocation);
            geoFire.setLocation(selectedUser, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
    }

}
