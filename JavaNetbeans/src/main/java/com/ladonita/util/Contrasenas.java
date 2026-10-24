package com.ladonita.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import com.google.common.base.Preconditions;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 + sal aleatoria (tomado del repositorio del equipo,
 * Util/Contrasenas.java). Formato guardado: pbkdf2$iteraciones$salBase64$hashBase64.
 *
 * Para generar un hash desde consola:  java -cp target/classes com.ladonita.util.Contrasenas MiClave
 */
public final class Contrasenas {

    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SALT = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Contrasenas() {
    }

    public static String hash(String contrasena) {
        Preconditions.checkArgument(contrasena != null && !contrasena.isEmpty(), "La contraseña no puede estar vacía");
        byte[] salt = new byte[BYTES_SALT];
        RANDOM.nextBytes(salt);
        byte[] h = derivar(contrasena, salt, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(h);
    }

    /**
     * Verifica la contraseña. Acepta también hashes SHA-256 hexadecimales de la versión anterior
     * del sistema (ver {@link #necesitaActualizar}) para poder migrarlos al iniciar sesión.
     */
    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        if (esHash(guardado)) {
            try {
                String[] p = guardado.split("\\$");
                int iter = Integer.parseInt(p[1]);
                byte[] salt = Base64.getDecoder().decode(p[2]);
                byte[] esperado = Base64.getDecoder().decode(p[3]);
                return MessageDigest.isEqual(esperado, derivar(contrasena, salt, iter));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        if (guardado.matches("[0-9a-fA-F]{64}")) {
            return MessageDigest.isEqual(sha256Hex(contrasena).getBytes(StandardCharsets.UTF_8),
                    guardado.toLowerCase().getBytes(StandardCharsets.UTF_8));
        }
        return false; // texto plano u otro formato: nunca se acepta
    }

    /** true si el valor guardado no es PBKDF2 y debe reemplazarse tras un login correcto. */
    public static boolean necesitaActualizar(String guardado) {
        return !esHash(guardado);
    }

    public static boolean esHash(String valor) {
        return valor != null && valor.matches("pbkdf2\\$\\d+\\$[A-Za-z0-9+/=]+\\$[A-Za-z0-9+/=]+");
    }

    /** Reglas mínimas para contraseñas nuevas. Devuelve el error o null si es válida. */
    public static String validarNueva(String clave) {
        if (clave == null || clave.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (!clave.matches(".*[A-Za-z].*") || !clave.matches(".*\\d.*")) {
            return "La contraseña debe combinar letras y números.";
        }
        return clave.length() > 100 ? "La contraseña es demasiado larga." : null;
    }

    private static byte[] derivar(String contrasena, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), salt, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible en esta JVM", e);
        }
    }

    private static String sha256Hex(String texto) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java com.ladonita.util.Contrasenas <contraseña>");
            return;
        }
        System.out.println(hash(args[0]));
    }
}
