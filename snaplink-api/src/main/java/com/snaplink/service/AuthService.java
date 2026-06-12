package com.snaplink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

/**
 * Wraps AWS Cognito Identity Provider SDK for user management.
 *
 * <p>Provides registration and authentication flows using the
 * {@code USER_PASSWORD_AUTH} flow for simplicity.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id:}")
    private String userPoolId;

    @Value("${aws.cognito.client-id:}")
    private String clientId;

    public AuthService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    /**
     * Registers a new user with email and password.
     *
     * @param email    the user's email (used as username)
     * @param password the user's password
     */
    public void register(String email, String password) {
        try {
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .password(password)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build()
                    )
                    .build();

            cognitoClient.signUp(request);

            // Auto-confirm for demo/portfolio purposes
            AdminConfirmSignUpRequest confirmRequest = AdminConfirmSignUpRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build();
            cognitoClient.adminConfirmSignUp(confirmRequest);

            log.info("User registered and confirmed: {}", email);
        } catch (UsernameExistsException e) {
            throw new IllegalArgumentException("A user with this email already exists");
        } catch (InvalidPasswordException e) {
            throw new IllegalArgumentException("Password does not meet requirements: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns JWT tokens.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return authentication result containing tokens
     */
    public AuthenticationResultType login(String email, String password) {
        try {
            InitiateAuthRequest request = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of(
                            "USERNAME", email,
                            "PASSWORD", password
                    ))
                    .build();

            InitiateAuthResponse response = cognitoClient.initiateAuth(request);

            log.info("User authenticated: {}", email);
            return response.authenticationResult();
        } catch (NotAuthorizedException e) {
            throw new IllegalArgumentException("Invalid email or password");
        } catch (UserNotFoundException e) {
            throw new IllegalArgumentException("User not found");
        } catch (UserNotConfirmedException e) {
            throw new IllegalArgumentException("User account is not confirmed");
        }
    }
}
