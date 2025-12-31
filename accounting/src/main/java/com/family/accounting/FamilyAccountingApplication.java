package com.family.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 家庭记账系统启动类
 */
@SpringBootApplication
@EnableScheduling
public class FamilyAccountingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyAccountingApplication.class, args);
    }
}
