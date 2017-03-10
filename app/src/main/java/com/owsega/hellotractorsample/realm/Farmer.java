package com.owsega.hellotractorsample.realm;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Holds a farmer object
 *
 * @author Seyi Owoeye. Created on 3/9/17.
 */
public class Farmer extends RealmObject {
    @Index
    private long id;
    private String name;
    private String phone;
    private double latitude;
    private double longitude;
    private double farmSize;
    private String image;

    public Farmer() {
        setId(System.nanoTime());
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

    public String getFarmSizeStr() {
        return farmSize + "ha";
    }

    public Farmer setFarmSize(double farmSize) {
        this.farmSize = farmSize;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Farmer setImage(String image) {
        this.image = image;
        return this;
    }

    public String getDescription() {
        return getPhone() + "\n" +
                getLatitude() + " " + getLongitude() + "\n" +
                "Farm size: " + getFarmSize();
    }
}
