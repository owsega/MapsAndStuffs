package com.owsega.hellotractorsample;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.owsega.hellotractorsample.realm.Farmer;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionUtils;

public class MapsActivity extends BaseActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .name("data")
                .deleteRealmIfMigrationNeeded()
                .build());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, FarmerDetailsActivity.class));
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Utils.verifyLocationPermissions(this);
        Utils.addDummyFarmers(realm);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng abuja = new LatLng(9.078875, 7.484294);
        mMap.addMarker(new MarkerOptions().position(abuja).title("Hello Tractor Inc"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(abuja));

        // add farmers' markers to map
        RealmResults<Farmer> farmers = realm.where(Farmer.class).findAll();
        LatLng location;
        for (Farmer farmer : farmers) {
            location = new LatLng(farmer.getLatitude(), farmer.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(String.valueOf(farmer.getId()))
                    .snippet(farmer.getDescription())
                    .draggable(false));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!PermissionUtils.isGranted(this, PermissionEnum.ACCESS_FINE_LOCATION)) return;
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            // todo do something with location
            Log.e("seyi","new Location received: " + location.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showBottomSheet(Long.valueOf(marker.getTitle()));
        return true;
    }

    private void showBottomSheet(Long farmerId) {

    }
}
