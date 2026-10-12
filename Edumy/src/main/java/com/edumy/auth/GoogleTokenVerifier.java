package com.edumy.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {
    private final String clientId;

    public GoogleTokenVerifier(@Value("${edumy.google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleIdentity verify(String token) {
        if (clientId.isBlank()) {
            throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE, "Google login is not configured.");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(clientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null || !Boolean.TRUE.equals(idToken.getPayload().getEmailVerified())) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "Invalid Google token.");
            }
            String email = idToken.getPayload().getEmail();
            Object nameClaim = idToken.getPayload().get("name");
            if (email == null || !(nameClaim instanceof String name) || name.isBlank()) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "Google token is missing required profile data.");
            }
            return new GoogleIdentity(email, name);
        } catch (GeneralSecurityException | IOException exception) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Google token validation failed.");
        }
    }
}
