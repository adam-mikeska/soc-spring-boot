package com.projekt.projekt.Services;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PayPalClient {
    @Value("${paypal.client.id}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String secretId;

    public PayPalHttpClient client() {
        PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId,secretId);

        PayPalHttpClient client = new PayPalHttpClient(environment);
        client.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(900));
        return client;
    }

}
