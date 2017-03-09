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
    private
    long id;
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

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getFarmSize() {
        return farmSize;
    }

    public void setFarmSize(double farmSize) {
        this.farmSize = farmSize;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return getPhone() + "\n" +
                getLatitude() + " " + getLongitude() + "\n" +
                "Farm size: " + getFarmSize();
    }
}
