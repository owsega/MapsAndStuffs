package com.owsega.hellotractorsample;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.owsega.hellotractorsample.model.Farmer;
import com.owsega.hellotractorsample.model.FarmerEntity;
import com.owsega.hellotractorsample.model.FarmerFields;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static com.owsega.hellotractorsample.FetchAddressIntentService.FARMER_EXTRA;

/**
 * Home Activity containing the maps and markers indicating current farmers on the app
 *
 * @author Owoeye Oluwaseyi
 */
public class MapsActivity extends BaseActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        RealmChangeListener<RealmResults<Farmer>> {

    private static final int DETAILS_RC = 5;
    private final Map<String, Marker> mMarkers = new ConcurrentHashMap<>();
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

        if (!kinvey.user().isUserLoggedIn()) kinvey.user().login(new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable error) {
            }

            @Override
            public void onSuccess(User result) {
                // todo save login result for reuse...
            }
        });

        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_maps);

        FarmerEntity.fetchFarmersIntoRealm(this);

        FrameLayout bottomSheetLayout = (FrameLayout) findViewById(R.id.bottom_sheet_wrappper);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        hideBottomSheet();
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
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

//        Farmer.addDummyFarmers(this, realm);
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
            startActivityForResult(new Intent(this, FarmerDetailsActivity.class), DETAILS_RC);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
        mMap.clear();
        for (Farmer farmer : farmers) {
            addFarmerMarker(farmer);
        }
    }

    private void addFarmerMarker(Farmer farmer) {
        LatLng location = new LatLng(farmer.getLatitude(), farmer.getLongitude());
        Marker oldMarker = mMarkers.put(farmer.getId(), mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(farmer.getId())
                .draggable(false)));
        if (oldMarker != null) oldMarker.remove();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        try {
            String farmerId = marker.getTitle();
            currentFarmer = realm.where(Farmer.class).equalTo(FarmerFields.ID, farmerId).findFirst();
            if (currentFarmer != null) showBottomSheet();
            else hideBottomSheet();
        } catch (Exception ignored) {
        }
        return true;
    }

    private void hideBottomSheet() {
        bottomSheetBehavior.setPeekHeight(0);
    }

    @OnClick(R.id.delete_btn)
    public void deleteFarmer() {
        if (currentFarmer != null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.delete_armer_confirmation, currentFarmer.getName()))
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    Farmer.deleteFarmer(MapsActivity.this, currentFarmer);
                                    mMarkers.get(currentFarmer.getId()).remove();
                                    hideBottomSheet();
                                }
                            })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    @OnClick(R.id.update_btn)
    public void updateFarmer() {
        if (currentFarmer != null) {
            startActivityForResult(new Intent(MapsActivity.this, FarmerDetailsActivity.class)
                    .putExtra(FARMER_EXTRA, currentFarmer.getId()), DETAILS_RC);
            hideBottomSheet();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(FARMER_EXTRA)) {
                String farmerId = data.getStringExtra(FARMER_EXTRA);
                if (TextUtils.isEmpty(farmerId)) return;
                Marker marker = mMarkers.get(farmerId);
                if (marker != null) marker.remove();
                Farmer farmer = realm.where(Farmer.class).equalTo(FarmerFields.ID, farmerId).findFirst();
                if (farmer != null) {
                    addFarmerMarker(farmer);
                }
            }
        }
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setPeekHeight(Utils.dpToPx(this, 80));
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
        addAllFarmersMarkers(farmers);
    }
}
