package com.corelabsplus.getmetheredriver.utils;

/**
 * Created by nissi on 3/24/18.
 */

public class Driver {
    public String name;
    public String license;
    public String type;
    public String phone;

    public Driver(String name, String license,String phone , String type) {
        this.name = name;
        this.license = license;
        this.type = type;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getLicense() {
        return license;
    }

    public String getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }
}
