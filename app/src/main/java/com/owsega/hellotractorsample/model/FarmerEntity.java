package com.owsega.hellotractorsample.model;

import android.util.Log;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.owsega.hellotractorsample.BaseActivity;

/**
 * Holds a farmer object for Kinvey
 *
 * @author Seyi Owoeye. Created on 3/9/17.
 */
public class FarmerEntity extends GenericJson {
    public static final String FARMERS = "farmers";

    @Key("_id")
    private String id;
    @Key(FarmerFields.NAME)
    private String name;
    @Key(FarmerFields.PHONE)
    private String phone;
    @Key(FarmerFields.LATITUDE)
    private double latitude;
    @Key(FarmerFields.LONGITUDE)
    private double longitude;
    @Key(FarmerFields.FARM_SIZE)
    private double farmSize;
    @Key(FarmerFields.IMAGE)
    private String image;
    @Key(FarmerFields.ADDRESS)
    private String address;

    public FarmerEntity() {
        setId(String.valueOf(System.nanoTime()));
    }

    public FarmerEntity(String id) {
        setId(id);
    }

    public static void deleteFarmer(BaseActivity ctx, final FarmerEntity farmer) {
        Log.e("seyi", " prepping to delete farmer");
        Client mKinveyClient = ctx.getKinvey();
        Query query = mKinveyClient.query().equals(FarmerFields.ID, String.valueOf(farmer.getId()));
        AsyncAppData<FarmerEntity> myevents = mKinveyClient.appData(FARMERS, FarmerEntity.class);
        myevents.delete(query, new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse kinveyDeleteResponse) {
                Log.e("seyi", "farmer delete success");
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("seyi", "farmer delete failed");
                Log.e("TAG", "failed to save event data", e);
            }
        });
    }


    public static void fetchFarmersIntoRealm(BaseActivity context) {
        Log.e("seyi", "prepping to fetch all farmers t0 realm");
        Client mKinveyClient = context.getKinvey();
        Query myQuery = mKinveyClient.query();
        myQuery.notEqual(FarmerFields.PHONE, "_");
        getEvents(context).get(myQuery, new KinveyListCallback<FarmerEntity>() {
            @Override
            public void onSuccess(FarmerEntity[] results) {
                Log.e("seyi", "farmer fetch successful " + results.length);
                Farmer.saveFarmers(results);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e("seyi", "farmer fetch failed");
                Log.e("TAG", "failed to fetchFarmersIntoRealm", error);
            }
        });
    }

    public static FarmerEntity getFarmerEntity(Farmer farmer) {
        return new FarmerEntity(farmer.getId())
                .setName(farmer.getName())
                .setLongitude(farmer.getLongitude())
                .setLatitude(farmer.getLatitude())
                .setId(String.valueOf(farmer.getId()))
                .setImage(farmer.getImage())
                .setFarmSize(farmer.getFarmSize())
                .setAddress(farmer.getAddress())
                .setPhone(farmer.getPhone());
    }

    public static Farmer getFarmer(FarmerEntity farmer) {
        return new Farmer()
                .setName(farmer.getName())
                .setLongitude(farmer.getLongitude())
                .setLatitude(farmer.getLatitude())
                .setId(farmer.getId())
                .setImage(farmer.getImage())
                .setAddress(farmer.getAddress())
                .setFarmSize(farmer.getFarmSize())
                .setPhone(farmer.getPhone());
    }

    private static AsyncAppData<FarmerEntity> getEvents(BaseActivity context) {
        return context.getKinvey().appData(FARMERS, FarmerEntity.class);
    }

    public static void saveFarmer(BaseActivity context, FarmerEntity farmer) {
        Log.e("seyi","prepping to save farmer");
        getEvents(context).save(farmer, new KinveyClientCallback<FarmerEntity>() {
            @Override
            public void onSuccess(FarmerEntity farmerEntity) {
                Log.e("seyi","success to save farmer");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("seyi","failed to save farmer");
            }
        });
    }

    public String getId() {
        return id;
    }

    public FarmerEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FarmerEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public FarmerEntity setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public FarmerEntity setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public FarmerEntity setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getFarmSize() {
        return farmSize;
    }

    public FarmerEntity setFarmSize(double farmSize) {
        this.farmSize = farmSize;
        return this;
    }

    public String getImage() {
        return image;
    }

    public FarmerEntity setImage(String image) {
        this.image = image;
        return this;
    }

    public String getLatLong() {
        return "(" + getLatitude() + "," + getLongitude() + ")";
    }

    public String getAddress() {
        return address;
    }

    public FarmerEntity setAddress(String address) {
        this.address = address;
        return this;
    }
}
