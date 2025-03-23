package server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mdanbari.shared.AesPayload;
import dev.mdanbari.shared.CryptoUtil;
import dev.mdanbari.shared.Profile;
import dev.mdanbari.shared.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private PrivateKey privateKey;

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        byte[] privateKeyInBytes = new ClassPathResource("private_key.der").getInputStream().readAllBytes();
        privateKey = CryptoUtil.loadPrivateKey(privateKeyInBytes);
    }

    @PostMapping("/profile")
    public String receive(@RequestBody RequestData req) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        byte[] decryptedKey = CryptoUtil.rsaDecrypt(Base64.getDecoder().decode(req.encryptedAesKey()), privateKey);
        var aesPayload = new AesPayload(
                Base64.getDecoder().decode(req.iv()),
                Base64.getDecoder().decode(req.encryptedData()));

        byte[] decrypted = CryptoUtil.aesDecrypt(aesPayload, new SecretKeySpec(decryptedKey, "AES"));
        Profile profile = new ObjectMapper().readValue(decrypted, Profile.class);
        logger.info("Received profile: {}", profile);
        return "Received: " + profile.name();
    }
}
