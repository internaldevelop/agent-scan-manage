package com.toolkit.scantaskmng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// for test github user login

@SpringBootApplication
@EnableScheduling
public class ScanTaskManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScanTaskManageApplication.class, args);
    }

}
