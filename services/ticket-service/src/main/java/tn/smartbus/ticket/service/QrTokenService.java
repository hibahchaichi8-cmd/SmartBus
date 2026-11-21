package tn.smartbus.ticket.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QrTokenService {

    private final byte[] secret;

    public QrTokenService(
            @Value("${app.qr-secret}") String configuredSecret) {

        try {
            secret = Base64.getDecoder().decode(configuredSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "QR_SECRET must be valid Base64"
            );
        }

        if (secret.length < 32) {
            throw new IllegalStateException(
                    "QR_SECRET must contain at least 32 decoded bytes"
            );
        }
    }

    public String generateToken(String ticketId) {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket identifier is required"
            );
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(secret, "HmacSHA256"));

            String content = "smartbus-ticket:" + ticketId;

            byte[] result = mac.doFinal(
                    content.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(result);

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to generate QR token",
                    exception
            );
        }
    }

    public String hashToken(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "QR token is required"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] result = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to calculate QR token hash",
                    exception
            );
        }
    }
}