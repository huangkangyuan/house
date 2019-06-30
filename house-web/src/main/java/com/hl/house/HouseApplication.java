package com.hl.house;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.autoconfig.house.autoconfig.EnableHttpClient;

@SpringBootApplication
@EnableHttpClient
@EnableAsync //开启异步代理模式
public class HouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouseApplication.class, args);
    }
}
