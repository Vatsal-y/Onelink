package com.snaplink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.net.URI;

/**
 * Configures the AWS Cognito Identity Provider client.
 */
@Configuration
public class CognitoConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.cognito.endpoint:}")
    private String endpoint;

    @Bean
    public CognitoIdentityProviderClient cognitoClient() {
        var builder = CognitoIdentityProviderClient.builder()
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}
