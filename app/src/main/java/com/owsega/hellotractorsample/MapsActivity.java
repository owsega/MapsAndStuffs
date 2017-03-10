package com.owsega.hellotractorsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.owsega.hellotractorsample.realm.Farmer;
import com.owsega.hellotractorsample.realm.FarmerFields;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static com.owsega.hellotractorsample.FetchAddressIntentService.FARMER_EXTRA;

public class MapsActivity extends BaseActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        RealmChangeListener<RealmResults<Farmer>> {

    private final Map<Long, Marker> mMarkers = new ConcurrentHashMap<Long, Marker>();
    @BindView(R.id.profile_pic)
    ImageView profile_pic;
    @BindView(R.id.header_wrapper)
    FrameLayout header_wrapper;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.phone)
    TextView phone;
    @BindView(R.id.farmSize)
    TextView farmSize;
    @BindView(R.id.textViewOptions)
    TextView textViewOptions;
    Farmer currentFarmer;
    private GoogleMap mMap;
    private BottomSheetBehavior bottomSheetBehavior;

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

        FrameLayout bottomSheetLayout = (FrameLayout) findViewById(R.id.bottom_sheet_wrappper);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
//                        finish();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                int initialHeight = 80;
                int finalHeight = 200;
                int heightDifference = finalHeight - initialHeight;
                float currentHeight = slideOffset * heightDifference;
                float scale = (initialHeight + currentHeight) / finalHeight;
                profile_pic.setScaleY(scale);

                int defaultPadding = Utils.dpToPx(MapsActivity.this, 16);
                int topPadding = defaultPadding + Utils.dpToPx(MapsActivity.this, (int) currentHeight);
                name.setPadding(defaultPadding, topPadding, defaultPadding, defaultPadding);
                name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 + slideOffset * 16);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Utils.addDummyFarmers(this, realm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            startActivity(new Intent(this, FarmerDetailsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        mMap.setOnMarkerClickListener(this);

        LatLng abuja = new LatLng(9.078875, 7.484294);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(abuja));

        // add farmers' markers to map
        RealmResults<Farmer> farmers = realm.where(Farmer.class).findAll();
        farmers.addChangeListener(this);
        addAllFarmersMarkers(farmers);
    }

    private void addAllFarmersMarkers(RealmResults<Farmer> farmers) {
        LatLng location;
        for (Farmer farmer : farmers) {
            location = new LatLng(farmer.getLatitude(), farmer.getLongitude());
            Marker oldMarker = mMarkers.put(farmer.getId(), mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(String.valueOf(farmer.getId()))
                    .snippet(farmer.getLatLong())
                    .draggable(false)));
            if (oldMarker != null) oldMarker.remove();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Long farmerId = Long.valueOf(marker.getTitle());
        currentFarmer = realm.where(Farmer.class).equalTo(FarmerFields.ID, farmerId).findFirst();
        showBottomSheet();
        return true;
    }

    @OnClick(R.id.delete_btn)
    public void deleteFarmer() {
        Utils.showDeleteFarmerDialog(this, realm, currentFarmer);
    }

    @OnClick(R.id.update_btn)
    public void updateFarmer() {
        startActivity(new Intent(MapsActivity.this, FarmerDetailsActivity.class)
                .putExtra(FARMER_EXTRA, currentFarmer.getId()));
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);

        try {
            name.setText(currentFarmer.getName());
            address.setText(currentFarmer.getLatLong());
            phone.setText(currentFarmer.getPhone());
            farmSize.setText(currentFarmer.getFarmSizeStr());
            Utils.loadProfilePic(this, profile_pic, currentFarmer.getImage());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onChange(RealmResults<Farmer> farmers) {
        Log.e("seyi","change is here!!!");
        addAllFarmersMarkers(farmers);
    }
}
