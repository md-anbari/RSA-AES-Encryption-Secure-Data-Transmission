package dev.mdanabri.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mdanbari.shared.CryptoUtil;
import dev.mdanbari.shared.Profile;
import dev.mdanbari.shared.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.Base64;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
    private static final String SERVER_URL = "http://localhost:8086/api/profile";

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {

        Profile profile = new Profile("Alice", "alice@example.com", "9387712929", 39, "Alice's address");
        byte[] json = new ObjectMapper().writeValueAsBytes(profile);

        SecretKey aesKey = KeyGenerator.getInstance("AES").generateKey();
        var aesPayload = CryptoUtil.aesEncrypt(json, aesKey);
        byte[] publicKeyInBytes = new ClassPathResource("public_key.der").getInputStream().readAllBytes();
        PublicKey publicKey = CryptoUtil.loadPublicKey(publicKeyInBytes);
        byte[] encryptedKey = CryptoUtil.rsaEncrypt(aesKey.getEncoded(), publicKey);

        WebClient.create(SERVER_URL).post()
                .bodyValue(new RequestData(
                        Base64.getEncoder().encodeToString(encryptedKey),
                        Base64.getEncoder().encodeToString(aesPayload.iv()),
                        Base64.getEncoder().encodeToString(aesPayload.ciphertext())))
                .retrieve().bodyToMono(String.class)
                .subscribe(response -> logger.info("Response: {}", response));
    }
}
