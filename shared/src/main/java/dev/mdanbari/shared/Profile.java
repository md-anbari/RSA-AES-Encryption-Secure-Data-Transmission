package dev.mdanbari.shared;

public record Profile(
        String name,
        String email,
        String phone,
        int age,
        String address
) {}