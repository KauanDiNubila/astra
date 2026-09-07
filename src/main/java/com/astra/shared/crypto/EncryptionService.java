package com.astra.shared.crypto;

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

// Criptografa dados sensiveis em repouso (AES-256-GCM) - protege contra
// vazamento do dump/credencial do banco. Usado hoje por mensagens/anexos de
// chat e por tokens de integracao (ex: GitHub). O servidor ainda processa o
// conteudo em claro em memoria quando precisa; isso nao e criptografia
// ponta-a-ponta.
@Component
public class EncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public EncryptionService(@Value("${astra.chat.encryption-key}") String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    // Dados salvos antes dessa funcionalidade existir ainda estao em texto
    // puro no banco - se a descriptografia falhar (nao e base64 valido, ou a
    // tag GCM nao bate), trata como legado e devolve como veio, em vez de
    // quebrar o carregamento. Chamadores que nao podem aceitar esse fallback
    // (ex: tokens de integracao) devem usar decryptStrict.
    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        try {
            return decryptStrict(stored);
        } catch (RuntimeException legacyPlaintext) {
            return stored;
        }
    }

    // Variante que nao aplica o fallback de "texto legado" - lanca se a
    // descriptografia falhar. Usado por integracoes (ex: token do GitHub)
    // onde um valor corrompido/chave rotacionada nunca deve ser tratado como
    // se tivesse funcionado.
    public String decryptStrict(String stored) {
        byte[] raw = Base64.getDecoder().decode(stored);
        return new String(decryptBytesStrict(raw), StandardCharsets.UTF_8);
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
            return decryptBytesStrict(stored);
        } catch (RuntimeException legacyPlaintext) {
            return stored;
        }
    }

    private byte[] decryptBytesStrict(byte[] stored) {
        try {
            byte[] iv = Arrays.copyOfRange(stored, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(stored, IV_LENGTH_BYTES, stored.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Falha ao descriptografar", e);
        }
    }
}
