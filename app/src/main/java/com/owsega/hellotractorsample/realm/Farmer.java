package com.owsega.hellotractorsample.realm;

import android.content.Context;

import com.owsega.hellotractorsample.BaseActivity;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

import static com.owsega.hellotractorsample.Utils.getFarmerAddress;

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

    public static void addDummyFarmers(final Context context, Realm realm) {
        long farmerCount = realm.where(Farmer.class).count();
        if (farmerCount < 25) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();

                    Farmer farmer = new Farmer()
                            .setLatitude(9.078875)
                            .setLatitude(7.484294)
                            .setName("Hello Tractor Inc")
                            .setFarmSize(0)
                            .setPhone("09096909999");
                    getFarmerAddress(context, realm, farmer);
                    realm.copyToRealmOrUpdate(farmer);
                    FarmerEntity.addDummyFarmers((BaseActivity) context);

                /*    for (int i = 0; i < 10; i++) {
                        farmer = new Farmer()
                                .setFarmSize(Math.random() * 120000)
                                .setLatitude(6 + Math.random() * 5)
                                .setLongitude(3 + Math.random() * 8)
                                .setName(names[(int) (Math.random() * names.length)] + " " +
                                        names[(int) (Math.random() * names.length)])
                                .setPhone("08106184121");
                        getFarmerAddress(context, realm, farmer);
                        realm.copyToRealmOrUpdate(farmer);
                    }*/
                }
            });
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
        return "(" + getLatitude() + "," + getLongitude() + ")";
    }

    public Farmer setAddress(String address) {
        this.address = address;
        return this;
    }
}
