package com.snaplink.handler;

import com.amazonaws.serverless.proxy.spring.SpringDelegatingLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AWS Lambda handler that delegates to Spring Boot via the
 * {@link SpringDelegatingLambdaContainerHandler}.
 *
 * <p>This handler uses the delegating approach which discovers the
 * Spring Boot application class automatically via the {@code MAIN_CLASS}
 * environment variable or classpath scanning.
 *
 * <p>With SnapStart enabled (configured in SAM template), the cold-start
 * penalty is reduced to < 200ms.
 */
public class StreamLambdaHandler implements RequestStreamHandler {

    private final SpringDelegatingLambdaContainerHandler handler;

    public StreamLambdaHandler() {
        try {
            handler = new SpringDelegatingLambdaContainerHandler();
        } catch (com.amazonaws.serverless.exceptions.ContainerInitializationException e) {
            throw new RuntimeException("Could not initialize Spring Boot for Lambda", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        handler.handleRequest(input, output, context);
    }
}
