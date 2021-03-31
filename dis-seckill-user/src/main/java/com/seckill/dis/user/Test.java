package com.seckill.dis.user;

import java.sql.Driver;
import java.util.Iterator;
import java.util.ServiceLoader;

public class Test {

    public static void main(String[] args) {
        ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
        Iterator<Driver> driversIterator = loadedDrivers.iterator();

        while (driversIterator.hasNext()) {
            Driver driver = driversIterator.next();
        }

    }
}
