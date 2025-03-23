package dev.mdanbari.shared;

public record AesPayload(
        byte[] iv,
        byte[] ciphertext
) {}
