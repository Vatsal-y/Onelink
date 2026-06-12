package com.snaplink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SnapLink — Serverless URL Shortener.
 *
 * Main entry point for local development.
 * In AWS Lambda, {@link com.snaplink.handler.StreamLambdaHandler} bootstraps
 * the Spring context instead.
 */
@SpringBootApplication
public class SnaplinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnaplinkApplication.class, args);
    }
}
