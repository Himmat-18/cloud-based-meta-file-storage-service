package org.example.cloud_storage.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private static final String SECRET =
            "CloudStorageSecretKeyForJwtAuthentication2026";

    // =========================
    // GENERATE TOKEN
    // =========================

    public String generateToken(
            Long userId,
            String email,
            String role) {

        String header = encode(
                "{\"alg\":\"HS256\",\"typ\":\"JWT\"}"
        );

        String payload = encode(
                "{\"userId\":" + userId +
                        ",\"email\":\"" + email +
                        "\",\"role\":\"" + role + "\"}"
        );

        String data = header + "." + payload;

        return data + "." + sign(data);
    }

    // =========================
    // VALIDATE TOKEN
    // =========================

    public boolean isTokenValid(String token) {

        try {

            if (token == null) {
                return false;
            }

            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return false;
            }

            String data = parts[0] + "." + parts[1];

            String expectedSignature = sign(data);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception e) {

            return false;
        }
    }

    // =========================
    // EXTRACT USER ID
    // =========================

    public Long extractUserId(String token) {

        String payload = getPayload(token);

        Pattern pattern = Pattern.compile(
                "\"userId\"\\s*:\\s*(\\d+)"
        );

        Matcher matcher = pattern.matcher(payload);

        if (matcher.find()) {

            return Long.parseLong(
                    matcher.group(1)
            );
        }

        throw new RuntimeException(
                "User ID not found in token"
        );
    }

    // =========================
    // EXTRACT EMAIL
    // =========================

    public String extractEmail(String token) {

        String payload = getPayload(token);

        Pattern pattern = Pattern.compile(
                "\"email\"\\s*:\\s*\"([^\"]*)\""
        );

        Matcher matcher = pattern.matcher(payload);

        if (matcher.find()) {

            return matcher.group(1);
        }

        throw new RuntimeException(
                "Email not found in token"
        );
    }

    // =========================
    // EXTRACT ROLE
    // =========================

    public String extractRole(String token) {

        String payload = getPayload(token);

        Pattern pattern = Pattern.compile(
                "\"role\"\\s*:\\s*\"([^\"]*)\""
        );

        Matcher matcher = pattern.matcher(payload);

        if (matcher.find()) {

            return matcher.group(1);
        }

        throw new RuntimeException(
                "Role not found in token"
        );
    }

    // =========================
    // GET PAYLOAD
    // =========================

    private String getPayload(String token) {

        if (token == null) {
            throw new RuntimeException(
                    "Token is null"
            );
        }

        String[] parts = token.split("\\.");

        if (parts.length != 3) {

            throw new RuntimeException(
                    "Invalid JWT token"
            );
        }

        try {

            byte[] decodedPayload =
                    Base64.getUrlDecoder().decode(parts[1]);

            return new String(
                    decodedPayload,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid JWT payload",
                    e
            );
        }
    }

    // =========================
    // BASE64 ENCODE
    // =========================

    private String encode(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    // =========================
    // SIGN TOKEN
    // =========================

    private String sign(String data) {

        try {

            Mac mac = Mac.getInstance(
                    "HmacSHA256"
            );

            SecretKeySpec key =
                    new SecretKeySpec(
                            SECRET.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );

            mac.init(key);

            byte[] signature =
                    mac.doFinal(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            signature
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "JWT generation failed",
                    e
            );
        }
    }
}