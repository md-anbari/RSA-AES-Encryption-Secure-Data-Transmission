
# RSA + AES Encryption: A Combination for Secure Profile Data Transmission

In this project, we use a combination of RSA and AES encryption to secure sensitive profile data during transmission. This combination is chosen because it combines the strengths of both algorithms, offering a balance between security and performance.

## Why RSA + AES?
The decision to use both RSA and AES encryption comes from the need to address the limitations of each when used in isolation:

- **RSA** is an asymmetric encryption algorithm that uses a pair of keys (public and private) to encrypt and decrypt data. It is ideal for securely exchanging small pieces of information, such as encryption keys. However, RSA has performance limitations when used to encrypt large datasets because the encryption and decryption processes are computationally expensive.

- **AES** , in contrast, is a symmetric encryption algorithm that uses the same key for both encryption and decryption. AES is highly efficient and is suitable for encrypting large amounts of data. However, the main challenge with AES is securely sharing the key between the sender and the receiver.

By using **RSA to encrypt the AES key** and **AES to encrypt the actual data** (in this case, the profile), we can take advantage of the speed of AES for large data encryption while still ensuring that the AES key is exchanged securely using RSA.

---

## Encryption Details: RSA and AES

### RSA
In this project, **RSA/ECB/OAEPWithSHA-256AndMGF1Padding** is used for RSA encryption. The `ECB` mode is used because it is a simple block-based encryption scheme. For RSA, this is typically acceptable since only the AES key is being encrypted. The `OAEPWithSHA-256AndMGF1Padding` padding scheme is used because it is more secure than older padding schemes such as PKCS#1. The OAEP method with SHA-256 provides resistance against various cryptographic attacks.

**RSA Encryption**:
```java
public static byte[] rsaEncrypt(byte[] data, PublicKey key) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
}
```

**RSA Decryption**:
```java
public static byte[] rsaDecrypt(byte[] data, PrivateKey key) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(data);
}
```

### AES
AES is used to encrypt the actual data. In this project, the **AES/GCM/NoPadding** mode is chosen. GCM (Galois/Counter Mode) is a secure and efficient mode of AES. The **NoPadding** scheme is used because GCM handles padding internally, and any extra padding would interfere with the algorithm.

**AES Encryption**:
```java
public static AesPayload aesEncrypt(byte[] data, SecretKey key) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
    cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
    byte[] ciphertext = cipher.doFinal(data);
    return new AesPayload(iv, ciphertext);
}
```

**AES Decryption**:
```java
public static byte[] aesDecrypt(AesPayload payload, SecretKey key) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, payload.iv()));
    return cipher.doFinal(payload.ciphertext());
}
```

### Why PEM and DER?
- **DER** is a binary format for encoding data structures and is often used for storing keys. In Java, DER is a preferred format for handling private/public keys because it is a binary format that Java's cryptography libraries handle efficiently.
- **PEM** is a text-based encoding format that is more common in contexts like certificates. PEM files are often used when human readability is needed.

In Java applications, **DER** is preferred for key storage as it is a compact and straightforward binary format. However, if the keys need to be accessed by other systems, PEM may be used, but extra handling is required to parse the text-based encoding.

### Secret Key Management
In a real-world scenario, secrets such as RSA private keys, should be securely stored in a vault, such as **HashiCorp Vault**, **AWS Secrets Manager**, or **Azure Key Vault**. These tools ensure secure access control, auditing, and key management, preventing the keys from being hardcoded into the application code.

---

## RSA Key Generation
To generate RSA keys, you can use tools like **OpenSSL**. Here's how you can generate RSA keys:

```bash
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in private_key.pem -out public_key.pem
```

The **2048-bit** key size is used for RSA because it provides a good balance between security and performance. Keys smaller than 2048 bits are considered weak, while larger keys lead to unnecessary computational overhead.

---

## Project Structure
The project follows a modular Maven structure:

```xml
<groupId>dev.mdanbari</groupId>
<artifactId>rsa-dsa-poc</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>
<modules>
    <module>shared</module>
    <module>client</module>
    <module>server</module>
</modules>
```

- **shared**: Contains utility classes, including cryptographic functions.
- **client**: The client application that encrypts the profile and sends it to the server.
- **server**: The server application that decrypts the profile and handles requests.

---

## Conclusion
In this project, we demonstrate how to securely encrypt and transmit profile data using a combination of RSA and AES encryption. By using RSA to encrypt the AES key and AES to encrypt the profile data, we achieve a balance of security and performance. The choice of encryption modes and padding ensures the integrity, confidentiality, and authenticity of the transmitted data. In real-world applications, secret management tools like HashiCorp Vault should be used to securely store sensitive keys.
