package dev.mdanbari.shared;

public record RequestData(
        String encryptedAesKey,
        String iv,
        String encryptedData
) {}
