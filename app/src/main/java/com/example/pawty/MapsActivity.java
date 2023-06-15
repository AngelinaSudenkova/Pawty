package com.example.pawty;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.pawty.Model.Coordinates;
import com.example.pawty.Model.User;
import com.google.android.gms.location.LocationRequest;

import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;

import com.example.pawty.databinding.ActivityMapsBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.pawty.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.Manifest;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings;

import java.util.HashMap;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    boolean isPermissionGranted;

    private FusedLocationProviderClient mLocationClient;
    private int GPS_REQUEST_CODE = 9001;


    private Coordinates myCoordinates;
    private String userId;
    DatabaseReference reference;
    HashMap<String, Marker> friendMarkers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        this.userId = intent.getStringExtra("userid");
        friendMarkers = new HashMap<>();



        checkMyPermission();
        if (isPermissionGranted) {
            if (isGPSenabled()) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                mLocationClient = LocationServices.getFusedLocationProviderClient(this);
                getCurrentLocation();
                if(myCoordinates !=null) {
                    LatLng latLng = new LatLng(myCoordinates.getLatitude(), myCoordinates.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                    mMap.moveCamera(cameraUpdate);
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        }


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);


    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

           // locationManager.requestLocationUpdates();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    myCoordinates = new Coordinates(latitude, longitude);
                    Log.e("Coo", myCoordinates.toString());
                    updateUserCoordinates(myCoordinates);
                    setFriendsMarkers();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        } else {
            Toast.makeText(this, "Turn on GPS permission for this app", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void goToLocation(double latitude, double longitude) {
        this.myCoordinates = new Coordinates(latitude, longitude);
    }

    private void checkMyPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(MapsActivity.this, "Permission is granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                isPermissionGranted = false;
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    private boolean isGPSenabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnable) {
            return true;
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Set permission")
                    .setMessage("GPS is required for this app to work. Please enable GPS.")
                    .setPositiveButton("Yes, I'll do it now!", ((dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    })).setCancelable(false)
                    .show();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQUEST_CODE) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerEnable) {
                Toast.makeText(this, "GPS is available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS is not available", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateUserCoordinates(Coordinates coordinates){
        reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(userId);
        reference.child("coordinates").setValue(coordinates);

    }

    private void setFriendsMarkers(){
        DatabaseReference friendsRef = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Friends").child(userId);

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                        String friendId = friendSnapshot.getKey();
                        reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(friendId);
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User friend = snapshot.getValue(User.class);
                                Coordinates friendCoordinates = friend.getCoordinates();
                                String imageUrl = friend.getImageURL();

                                String friendId = friendSnapshot.getKey();
                                LatLng location = new LatLng(friendCoordinates.getLatitude(), friendCoordinates.getLongitude());

                                if (friendMarkers.containsKey(friendId)) {
                                    Marker marker = friendMarkers.get(friendId);
                                    marker.setPosition(location);
                                } else {
//                                    // Create a new marker
//                                    Picasso.get().load(imageUrl).into(new Target() {
//                                        @Override
//                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                            Bitmap resizeIcon = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
//                                            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resizeIcon);
//
//                                            Marker marker = mMap.addMarker(new MarkerOptions()
//                                                    .position(location)
//                                                    .icon(icon)
//                                                    .anchor(0.5f, 1.0f));
//
//                                            friendMarkers.put(friendId, marker);
//                                        }
//
//                                        @Override
//                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                                            Marker marker = mMap.addMarker(new MarkerOptions()
//                                                    .position(location));
//
//                                            friendMarkers.put(friendId, marker);
//                                        }
//
//
//                                        @Override
//                                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                        }
//                                    });

                                    RequestOptions requestOptions = new RequestOptions()
                                            .circleCrop(); // Apply circular cropping

                                    Marker userMarker = mMap.addMarker(new MarkerOptions()
                                                    .position(location)
                                                    .anchor(0.5f, 1.0f));

                                            friendMarkers.put(friendId, userMarker);

                                    Glide.with(MapsActivity.this)
                                            .asBitmap()
                                            .load(imageUrl)
                                            .apply(requestOptions)
                                            .into(new CustomTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, 150, 150, false);
                                                    Bitmap circularUserPhoto = createCircularBitmap(resizedBitmap);
                                                    userMarker.setIcon(BitmapDescriptorFactory.fromBitmap(circularUserPhoto));
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                                }
                                            });

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private Bitmap createCircularBitmap(Bitmap bitmap) {
        int diameter = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap circularBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float radius = diameter / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        Paint strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);

        canvas.drawCircle(radius, radius, radius - 2f, strokePaint);

        return circularBitmap;
    }

}