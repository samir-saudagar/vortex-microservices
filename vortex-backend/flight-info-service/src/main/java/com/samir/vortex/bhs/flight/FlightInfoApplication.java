package com.samir.vortex.bhs.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class FlightInfoApplication {
    public static void main(String[] args) {
        System.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "com.samir.vortex.bhs.flight.avro");
        SpringApplication.run(FlightInfoApplication.class, args);
    }
}
