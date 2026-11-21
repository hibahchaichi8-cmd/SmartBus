package tn.smartbus.auth.security;

import com.nimbusds.jose.jwk.*;
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SigningKeyProvider {
  private final RSAKey key;

  public SigningKeyProvider(@Value("${KEY_FILE:.data/keys/signing-key.pk8}") String filename)
      throws Exception {
    Path path = Path.of(filename);
    Files.createDirectories(path.toAbsolutePath().getParent());
    RSAPrivateCrtKey privateKey;
    if (Files.exists(path))
      privateKey =
          (RSAPrivateCrtKey)
              KeyFactory.getInstance("RSA")
                  .generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(path)));
    else {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      privateKey = (RSAPrivateCrtKey) generator.generateKeyPair().getPrivate();
      Files.write(path, privateKey.getEncoded(), StandardOpenOption.CREATE_NEW);
    }
    RSAPublicKey publicKey =
        (RSAPublicKey)
            KeyFactory.getInstance("RSA")
                .generatePublic(
                    new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
    String kid =
        java.util.HexFormat.of()
            .formatHex(MessageDigest.getInstance("SHA-256").digest(publicKey.getEncoded()));
    key = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(kid).build();
  }

  public RSAKey signingKey() {
    return key;
  }

  public java.util.Map<String, Object> publicJwks() {
    return new JWKSet(key.toPublicJWK()).toJSONObject();
  }
}
