package com.vs.netfixeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class NetflixEurekaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetflixEurekaServiceApplication.class, args);
    }
}
