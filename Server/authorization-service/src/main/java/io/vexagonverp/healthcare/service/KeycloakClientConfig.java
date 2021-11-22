package io.vexagonverp.healthcare.service;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KeycloakClientConfig {

    private final String adminClient;
    private final String adminRealm;
    private final String adminSecret;
    private final String keycloakServerUrl;

    @Autowired
    public KeycloakClientConfig(@Value("${keycloak.auth-server-url}") String keycloakServerUrl,
                                @Value("${healthcare.admin.client}") String adminClient,
                                @Value("${healthcare.admin.realm}") String adminRealm,
                                @Value("${healthcare.admin.secret}") String adminSecret) {
        this.keycloakServerUrl = keycloakServerUrl;
        this.adminClient = adminClient;
        this.adminRealm = adminRealm;
        this.adminSecret = adminSecret;
    }

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .serverUrl(keycloakServerUrl)
                .realm(adminRealm)
                .clientId(adminClient)
                .clientSecret(adminSecret)
                .build();
    }
}
