package io.vexagonverp.healthcare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.vexagonverp.healthcare.service.KeycloakTokenService;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import static io.vexagonverp.healthcare.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

@RestController
@RequestMapping("/api")
public class AuthorizationServiceController {

    private final KeycloakRestTemplate restTemplate;
    private final String keycloakServerUrl;
    private final String keycloakRealm;
    private final KeycloakTokenService keycloakTokenService;

    @Autowired
    public AuthorizationServiceController(KeycloakRestTemplate restTemplate,
                                   @Value("${keycloak.auth-server-url}") String keycloakServerUrl,
                                   @Value("${keycloak.realm}") String keycloakRealm,
                                   KeycloakTokenService keycloakTokenService) {
        this.restTemplate = restTemplate;
        this.keycloakServerUrl = keycloakServerUrl;
        this.keycloakRealm = keycloakRealm;
        this.keycloakTokenService = keycloakTokenService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        String token = "Bearer " + keycloakTokenService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(URI.create(keycloakServerUrl + "/admin/realms/" + keycloakRealm + "/users"),
                HttpMethod.GET, entity, List.class);
    }

    @Operation(summary = "Get string from public endpoint")
    @GetMapping("/public")
    public String getPublicString() {
        return "It is public.\n";
    }

    @Operation(
            summary = "Get string from private/secured endpoint",
            security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/private")
    public String getPrivateString(Principal principal) {
        return String.format("%s, it is private.%n", principal.getName());
    }
}