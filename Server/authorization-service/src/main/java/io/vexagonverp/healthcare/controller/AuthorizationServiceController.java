package io.vexagonverp.healthcare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.vexagonverp.healthcare.config.KeycloakClientConfig;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<?> users() {
        List<?> userRequest = keycloakClientConfig.keycloakApi().realm(keycloakRealm).users().list();
        return new ResponseEntity<>(userRequest, HttpStatus.OK);
    }

    @Operation(
            summary = "Verify user's email",
            security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @GetMapping("/verify")
    public ResponseEntity<?> users(Principal principal) {
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) principal;
        AccessToken accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();
        keycloakClientConfig.keycloakApi().realm(keycloakRealm).users().get(accessToken.getSubject()).sendVerifyEmail();
        return new ResponseEntity<>(accessToken,HttpStatus.OK);
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