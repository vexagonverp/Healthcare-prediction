package io.vexagonverp.healthcare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.vexagonverp.healthcare.service.KeycloakClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

import static io.vexagonverp.healthcare.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

@RestController
@RequestMapping("/api")
public class AuthorizationServiceController {

    private final String keycloakRealm;
    private final KeycloakClientConfig keycloakClientConfig;

    @Autowired
    public AuthorizationServiceController(@Value("${keycloak.realm}") String keycloakRealm,
                                          KeycloakClientConfig keycloakClientConfig) {
        this.keycloakRealm = keycloakRealm;
        this.keycloakClientConfig = keycloakClientConfig;
    }

    @GetMapping("/users")
    public List users() {
//        String token = "Bearer " + keycloakTokenService.getAccessToken();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", token);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
        return keycloakClientConfig.keycloak().realm(keycloakRealm).users().list();
//        return restTemplate.exchange(URI.create(keycloakServerUrl + "/admin/realms/" + keycloakRealm + "/users"),
//                HttpMethod.GET, entity, List.class);
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