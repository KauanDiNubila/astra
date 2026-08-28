package com.astra.chat.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Criptografa mensagens e anexos em repouso (AES-256-GCM) — protege contra
// vazamento do dump/credencial do banco. O servidor ainda processa o
// conteudo em claro em memoria pra enviar por WebSocket etc; isso nao e
// criptografia ponta-a-ponta.
@Component
public class ChatEncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public ChatEncryptionService(@Value("${astra.chat.encryption-key}") String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    // Mensagens salvas antes dessa funcionalidade existir ainda estao em
    // texto puro no banco — se a descriptografia falhar (nao e base64
    // valido, ou a tag GCM nao bate), trata como legado e devolve como
    // veio, em vez de quebrar o carregamento do historico.
    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(stored);
            return new String(decryptBytes(raw), StandardCharsets.UTF_8);
        } catch (RuntimeException legacyPlaintext) {
            return stored;
        }
    }

    public byte[] encryptBytes(byte[] plaintext) {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        random.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao criptografar", e);
        }
    }

    public byte[] decryptBytes(byte[] stored) {
        try {
            byte[] iv = Arrays.copyOfRange(stored, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(stored, IV_LENGTH_BYTES, stored.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException | IllegalArgumentException legacyPlaintext) {
            return stored;
        }
    }
}
