package com.owsega.hellotractorsample.realm;

import android.content.Context;

import com.owsega.hellotractorsample.BaseActivity;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Holds a farmer object
 *
 * @author Seyi Owoeye. Created on 3/9/17.
 */
public class Farmer extends RealmObject {
    @Index
    @PrimaryKey
    private long id;
    private String name;
    private String phone;
    private double latitude;
    private double longitude;
    private double farmSize;
    private String image;
    private String address;

    public Farmer() {
        setId(System.nanoTime());
    }

    public static void deleteFarmer(Context ctx, final Farmer farmer) {
        FarmerEntity.deleteFarmer((BaseActivity) ctx, FarmerEntity.getFarmerEntity(farmer));
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    farmer.deleteFromRealm();
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static void saveFarmers(final FarmerEntity[] results) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (FarmerEntity entity : results) {
                        realm.copyToRealmOrUpdate(FarmerEntity.getFarmer(entity));
                    }
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public long getId() {
        return id;
    }

    public Farmer setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Farmer setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Farmer setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public Farmer setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public Farmer setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getFarmSize() {
        return farmSize;
    }

    public Farmer setFarmSize(double farmSize) {
        this.farmSize = farmSize;
        return this;
    }

    public String getFarmSizeStr() {
        return farmSize + " hectares";
    }

    public String getImage() {
        return image;
    }

    public Farmer setImage(String image) {
        this.image = image;
        return this;
    }

    public String getLatLong() {
        return getAddress() != null ? getAddress()
                : "(" + getLatitude() + "," + getLongitude() + ")";
    }

    public String getAddress() {
        return address;
    }

    public Farmer setAddress(String address) {
        this.address = address;
        return this;
    }
}
