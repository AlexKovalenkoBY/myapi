package com.wbozon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;



@Slf4j
@SpringBootApplication
@EnableScheduling
@ComponentScan
@EntityScan
public class WbApi implements CommandLineRunner {

    private final BuildProperties buildProperties;
    private final ApplicationContext applicationContext;

    public WbApi(BuildProperties buildProperties, 
                           ApplicationContext applicationContext) {
        this.buildProperties = buildProperties;
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        SpringApplication.run(WbApi.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
      System.out.println("hi!");
        // throw new UnsupportedOperationException("Unimplemented method 'run'");
    }


}