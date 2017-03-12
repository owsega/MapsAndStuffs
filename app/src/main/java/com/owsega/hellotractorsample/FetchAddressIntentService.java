package com.owsega.hellotractorsample;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.owsega.hellotractorsample.model.Farmer;
import com.owsega.hellotractorsample.model.FarmerEntity;
import com.owsega.hellotractorsample.model.FarmerFields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

import static com.owsega.hellotractorsample.model.FarmerEntity.FARMERS;

/**
 * Reverse Geocoding service for turning latlng coordinates to addresses when a farmer is being saved
 *
 * @author Seyi Owoeye. Created on 3/10/17.
 */
public class FetchAddressIntentService extends IntentService {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String LOCATION_DATA_EXTRA = "locationData";
    public static final String FARMER_EXTRA = "FARMER_ID";
    private static final String TAG = "FetchAddressService";

    protected String farmerId;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        String errorMessage = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        farmerId = intent.getStringExtra(FARMER_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid latitude and longitude";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        } catch (Exception e) {
            // Catch other exceptions
            e.printStackTrace();
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address Found!");
            deliverResultToReceiver(SUCCESS_RESULT, TextUtils.join(", ", addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, final String address) {

        if (resultCode == FAILURE_RESULT) {
            Log.e(TAG, address);
            return;
        }

        saveAddressToKinvey(address);
        // save to realm
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(Farmer.class)
                            .equalTo(FarmerFields.ID, farmerId)
                            .findFirst()
                            .setAddress(address);
                }
            });
        } catch (Exception ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void saveAddressToKinvey(final String address) {
        final Client kinvey = new Client.Builder(getApplicationContext()).build();
        Query myQuery = kinvey.query().equals(FarmerFields.ID, farmerId);
        kinvey.appData(FARMERS, FarmerEntity.class)
                .get(myQuery, new KinveyListCallback<FarmerEntity>() {
                    @Override
                    public void onSuccess(FarmerEntity[] results) {
                        if (results.length < 1) {
                            Log.e(TAG, "farmer with given id not found");
                            return;
                        }
                        FarmerEntity farmer = results[0];
                        farmer.setAddress(address);
                        kinvey.appData(FARMERS, FarmerEntity.class)
                                .save(farmer, new KinveyClientCallback<FarmerEntity>() {
                                    @Override
                                    public void onSuccess(FarmerEntity farmerEntity) {
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable) {
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        Log.e("TAG", "failed to fetch farmer in order to save the id", error);
                    }
                });
    }
}
