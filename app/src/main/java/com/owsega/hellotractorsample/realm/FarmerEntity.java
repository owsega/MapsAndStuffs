package com.owsega.hellotractorsample.realm;

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
 * Holds a farmer object
 *
 * @author Seyi Owoeye. Created on 3/9/17.
 */
public class FarmerEntity extends GenericJson {
    public static final String FARMERS = "farmers";
    @Key("_id")
    private long id;
    @Key
    private String name;
    @Key
    private String phone;
    @Key
    private double latitude;
    @Key
    private double longitude;
    @Key
    private double farmSize;
    @Key
    private String image;
    @Key
    private String address;

    public FarmerEntity() {
        setId(System.nanoTime());
    }

    public static void deleteFarmer(BaseActivity ctx, final FarmerEntity farmer) {
        Client mKinveyClient = ctx.getKinvey();
        Query query = mKinveyClient.query().equals(FarmerFields.ID, farmer.getId());
        AsyncAppData<FarmerEntity> myevents = mKinveyClient.appData(FARMERS, FarmerEntity.class);
        myevents.delete(query, new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse kinveyDeleteResponse) {
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("TAG", "failed to save event data", e);
            }
        });
    }


    public static void fetchFarmersIntoRealm(BaseActivity context) {
        Client mKinveyClient = context.getKinvey();
        Query myQuery = mKinveyClient.query();
        myQuery.notEqual(FarmerFields.NAME, null);
        getEvents(context).get(myQuery, new KinveyListCallback<FarmerEntity>() {
            @Override
            public void onSuccess(FarmerEntity[] results) {
                Farmer.saveFarmers(results);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e("TAG", "failed to fetchByFilterCriteria", error);
            }
        });
    }

    public static FarmerEntity getFarmerEntity(Farmer farmer) {
        return new FarmerEntity()
                .setName(farmer.getName())
                .setLongitude(farmer.getLongitude())
                .setLatitude(farmer.getLatitude())
                .setId(farmer.getId())
                .setImage(farmer.getImage())
                .setFarmSize(farmer.getFarmSize())
                .setPhone(farmer.getPhone());
    }

    public static Farmer getFarmer(FarmerEntity farmer) {
        return new Farmer()
                .setName(farmer.getName())
                .setLongitude(farmer.getLongitude())
                .setLatitude(farmer.getLatitude())
                .setId(farmer.getId())
                .setImage(farmer.getImage())
                .setFarmSize(farmer.getFarmSize())
                .setPhone(farmer.getPhone());
    }

    private static AsyncAppData<FarmerEntity> getEvents(BaseActivity context) {
        return context.getKinvey().appData(FARMERS, FarmerEntity.class);
    }

    public static void addDummyFarmers(BaseActivity context) {
        FarmerEntity farmer = new FarmerEntity()
                .setLatitude(9.078875)
                .setLatitude(7.484294)
                .setName("Hello Tractor Inc")
                .setFarmSize(0)
                .setPhone("09096909999");
        saveFarmer(context, farmer);
    }

    public static void saveFarmer(BaseActivity context, FarmerEntity farmer) {
        getEvents(context).save(farmer, new KinveyClientCallback<FarmerEntity>() {
            @Override
            public void onSuccess(FarmerEntity farmerEntity) {
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    public long getId() {
        return id;
    }

    public FarmerEntity setId(long id) {
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

    public String getFarmSizeStr() {
        return farmSize + " hectares";
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

    public FarmerEntity setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getAddress() {
        return address;
    }
}
