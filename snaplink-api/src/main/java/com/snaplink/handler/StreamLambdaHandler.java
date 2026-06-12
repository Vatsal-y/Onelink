package com.snaplink.handler;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.snaplink.SnaplinkApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AWS Lambda handler that proxies API Gateway requests to the Spring Boot application.
 *
 * The static initializer block bootstraps the Spring context once during the
 * Lambda cold start. Subsequent invocations reuse the warm context, giving
 * sub-10ms handler overhead on warm starts.
 *
 * With SnapStart enabled (configured in SAM template), the cold-start penalty
 * is reduced to < 200ms.
 */
public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringLambdaContainerHandler.getAwsProxyHandler(SnaplinkApplication.class);
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Could not initialize Spring Boot application for Lambda", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        handler.proxyStream(input, output, context);
    }
}
