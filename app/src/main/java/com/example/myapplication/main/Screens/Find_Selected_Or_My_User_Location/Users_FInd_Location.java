package com.example.myapplication.main.Screens.Find_Selected_Or_My_User_Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Online_Offline_Service;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Users_FInd_Location extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //todo: main staff
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationUpdateActive;

    //todo: references
    private DatabaseReference userDataLocation;
    private String currentUserId;
    private String userFromIntentId;

    //todo: additional staff
    private Button findSelectedUserBtn, findNearestUserBtn;
    private int radiusOfSearch = 2;
    private boolean isNearestUserFound = false;
    private String nearestUserId;
    private ImageView line;

    private Marker nearestUserMarker;
    private Marker selectedUserMarker;

    //todo: trim number
    private static final DecimalFormat df = new DecimalFormat("0.00");


    //todo: from library (work only with billing)
    private List<Polyline> polylines=null;
    private LatLng exMyLatLng;
    private LatLng exHisLatLng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_find_location_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //todo: Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initialization();

        updateUserStatus("online");

        buildLocationRequest();

        buildLocationCallback();

        buildLocationSettingsRequest();

        startLocationUpdate();

        getIncomeIntent();

        initFindSelectedUser();

    }

    private void  initialization(){
        findSelectedUserBtn = findViewById(R.id.changeBtnInLocation);
        findNearestUserBtn = findViewById(R.id.findBtnInLocation);
        line = findViewById(R.id.lineInFindsLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        userDataLocation = FirebaseDatabase.getInstance().getReference().child("Users_Locations");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    //todo: main methods to setup location
    private void buildLocationRequest() {
        locationRequest = LocationRequest.create().setInterval(10000).setFastestInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    private void updateLocationUi() {

        if(currentLocation != null){
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Your location"));

            findSelectedUserBtn.setEnabled(true);
            findNearestUserBtn.setEnabled(true);

            GeoFire geoFire = new GeoFire(userDataLocation);
            geoFire.setLocation(currentUserId, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }

    }

    //todo: main switch methods
    private void stopLocationUpdates() {

        if(!isLocationUpdateActive){
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isLocationUpdateActive = false;
            }
        });

    }

    private void startLocationUpdate() {

        isLocationUpdateActive = true;

        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                //add automatically , dont copy!
                if (ActivityCompat.checkSelfPermission(Users_FInd_Location.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (Users_FInd_Location.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }


                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                updateLocationUi();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                //переводим оишбку в ошибку api
                int statusCode = ((ApiException) e).getStatusCode();

                switch (statusCode){
                    //если пользователь не дал разрешение - добавляем разрешение (ЕСЛИ ЭТО МОЖНО СДЕЛАТЬ ИЗ ПРИЛОЖЕНИЯ)
                    case LocationSettingsStatusCodes
                            .RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(Users_FInd_Location.this, App_Constants.CHECK_SETTINGS_CODE);
                        }catch (IntentSender.SendIntentException ignored){}
                        break;
                    //невозможно изменить настройки из приложения
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(Users_FInd_Location.this, "Adjust location settings in your device", Toast.LENGTH_LONG).show();
                        isLocationUpdateActive = false;
                }

                updateLocationUi();

            }
        });

    }

    //todo: check is current user
    private void getIncomeIntent() {
        Intent intentFromProfile = getIntent();
        userFromIntentId = intentFromProfile.getStringExtra("selectedUser");

        if(userFromIntentId.equals(currentUserId)){
            findSelectedUserBtn.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
            findNearestUserBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findNearestUserBtn.setText(String.valueOf("Finding.."));
                    gettingNearestUserLocation();
                }
            });
        }
        else{
            findSelectedUserBtn.setVisibility(View.VISIBLE);
        }
    }

    //todo: get selected user
    private void initFindSelectedUser() {
        findSelectedUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findSelectedUserBtn.setText(R.string.finding_location);
                gettingSelectedUserLocation();
            }
        });
    }

    private void gettingSelectedUserLocation() {

        DatabaseReference selectedUserLocation = FirebaseDatabase.getInstance().getReference("Users_Locations").child(userFromIntentId).child("l");

        selectedUserLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    List<Object> usersLocationParameters = (List<Object>) snapshot.getValue();
                    double latitude = 0;
                    double longitude = 0;

                    if(usersLocationParameters.get(0) != null){
                        latitude = Double.parseDouble(usersLocationParameters.get(0).toString());
                    }

                    if(usersLocationParameters.get(1) != null){
                        longitude = Double.parseDouble(usersLocationParameters.get(1).toString());
                    }

                    LatLng selectedUserLatLng = new LatLng(latitude, longitude);

                    if(selectedUserMarker != null){
                        selectedUserMarker.remove();
                    }

                    Location selectedUserLocation = new Location("");
                    selectedUserLocation.setLatitude(latitude);
                    selectedUserLocation.setLongitude(longitude);

                    float distanceToDriver = selectedUserLocation.distanceTo(currentLocation);
                    boolean isKm = false;
                    if(distanceToDriver > 1000){
                        distanceToDriver = distanceToDriver/1000;
                        isKm = true;
                    }

                    selectedUserMarker = mMap.addMarker(new MarkerOptions().position(selectedUserLatLng).title("This User"));

                    if(isKm){
                        findSelectedUserBtn.setText(String.valueOf(R.string.Distance + df.format(distanceToDriver) + "km"));
                    }
                    else{
                        findSelectedUserBtn.setText(String.valueOf(R.string.Distance + df.format(distanceToDriver) + R.string.m));
                    }

                    LatLng myLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    createRoutes(myLatLng, selectedUserLatLng);

                    //todo: for block of create path
                    exMyLatLng = myLatLng;
                    exHisLatLng = selectedUserLatLng;
                   // Findroutes(myLatLng, selectedUserLatLng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //todo: get nearest user
    private void gettingNearestUserLocation() {

        GeoFire geoFire = new GeoFire(userDataLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()),radiusOfSearch);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!isNearestUserFound && !key.equals(currentUserId)){
                    isNearestUserFound = true;
                    nearestUserId = key;

                    getNearestUserLocation();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!isNearestUserFound){
                    radiusOfSearch++;
                    gettingNearestUserLocation();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void getNearestUserLocation() {

        findNearestUserBtn.setText(R.string.finding_location);

        DatabaseReference nearestUserLocation = FirebaseDatabase.getInstance().getReference("Users_Locations").child(nearestUserId).child("l");

        nearestUserLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    List<Object> usersLocationParameters = (List<Object>) snapshot.getValue();
                    double latitude = 0;
                    double longitude = 0;

                    if(usersLocationParameters.get(0) != null){
                        latitude = Double.parseDouble(usersLocationParameters.get(0).toString());
                    }

                    if(usersLocationParameters.get(1) != null){
                        longitude = Double.parseDouble(usersLocationParameters.get(1).toString());
                    }

                    LatLng nearestUserLatLng = new LatLng(latitude, longitude);

                    if(nearestUserMarker != null){
                        nearestUserMarker.remove();
                    }

                    Location nearestUserLocation = new Location("");
                    nearestUserLocation.setLatitude(latitude);
                    nearestUserLocation.setLongitude(longitude);

                    float distanceToDriver = nearestUserLocation.distanceTo(currentLocation);

                    nearestUserMarker = mMap.addMarker(new MarkerOptions().position(nearestUserLatLng).title("This User"));

                    findNearestUserBtn.setText(String.valueOf(R.string.Distance + df.format(distanceToDriver) + R.string.m));

                    LatLng myLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    createRoutes(myLatLng, nearestUserLatLng);

                    //for block of create path
                    exMyLatLng = myLatLng;
                    exHisLatLng = nearestUserLatLng;
                   // Findroutes(myLatLng, nearestUserLatLng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //todo: draw routes
    private void createRoutes(LatLng myLatLng, LatLng driverLatLng) {

        Polyline line = mMap.addPolyline(
                new PolylineOptions().add(
                        myLatLng,
                        driverLatLng
                ).width(2).color(Color.RED).geodesic(true));
    }

    //block create path with routes( only wtih billing)
    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(Users_FInd_Location.this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyCrO_t3Wgz0d0eU-mRijHM1P9GJx9EmgLk")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //todo: Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(Users_FInd_Location.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //todo: If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(exMyLatLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {
                //in process
            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(exMyLatLng,exHisLatLng);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Findroutes(exMyLatLng,exHisLatLng);

    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mMap = googleMap;

       /* googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + ":" + latLng.longitude);
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                mMap.addMarker(markerOptions);
            }
        });

        */

        // Add a marker in Sydney and move the camera
       /* if(currentLocation != null){
            LatLng driverLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLocation));
        }

        */
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case App_Constants.CHECK_SETTINGS_CODE:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        Log.d("Users_find_location", "User has agreed to change location settings");
                        startLocationUpdate();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("Users_find_location", "User has not agreed to change location settings");
                        isLocationUpdateActive = false;

                        updateLocationUi();
                        break;
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");

        if(isLocationUpdateActive && checkLocationPermission()){

            startLocationUpdate();
        }
        else if(!checkLocationPermission()){
            requestLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
        stopLocationUpdates();
    }

    private void requestLocationPermission() {

        //должны лт пользователя обьяснять зачем трубем разрещение
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(shouldProvideRationale){
            // если пользователь уже отказал нам в разрещение - предоставляем рациональное обьяснение
            showSnackBar(
                    "Location permission is needed for" + " app functionality", "OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //когда пользователь нажмет на ОК
                            ActivityCompat.requestPermissions(Users_FInd_Location.this, new String []{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            }, App_Constants.REQEST_LOCATION_PERMISSION);
                        }
                    }
            );
        }
        else{
            ActivityCompat.requestPermissions(Users_FInd_Location.this, new String []{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, App_Constants.REQEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == App_Constants.REQEST_LOCATION_PERMISSION) {

            if (grantResults.length <= 0) {
                Log.d("PermissionsResult", "Request was cancelled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationUpdateActive) {
                    startLocationUpdate();
                }
            } else {
                showSnackBar("Turn on location on settings", "Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //переходим в настройки для добавления общего разрешение на геолокацию
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE).setAction(action, listener).show();
    }

    private boolean checkLocationPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;

    }

    //todo: block online/offline and location
    public void updateUserStatus( String state){
        Online_Offline_Service.updateUserStatus(state, this);
    }
}