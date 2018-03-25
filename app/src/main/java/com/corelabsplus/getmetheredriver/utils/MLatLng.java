package com.corelabsplus.getmetheredriver.utils;

/**
 * Created by nissi on 3/24/18.
 */

public class MLatLng {
    private double latitude;
    private double longitude;

    public MLatLng(){}

    public MLatLng(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
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
}
