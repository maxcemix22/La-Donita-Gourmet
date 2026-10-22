package Util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 (incluido en Java, sin librerías externas).
 * Formato guardado: pbkdf2$iteraciones$salt$hash (Base64). Cabe en la columna contraseña (varchar 255).
 *
 * Para generar un hash desde consola:  java -cp build/classes Util.Contrasenas MiClave123
 */
public final class Contrasenas {
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SALT = 16;
    private static final int BITS_HASH = 256;

    private Contrasenas() {}

    public static String hash(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) throw new IllegalArgumentException("La contraseña no puede estar vacía");
        byte[] salt = new byte[BYTES_SALT];
        new SecureRandom().nextBytes(salt);
        byte[] h = derivar(contrasena, salt, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(h);
    }

    /** true solo si la contraseña corresponde al hash guardado. Un valor con otro formato (ej. texto plano) siempre da false. */
    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || !esHash(guardado)) return false;
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

    public static boolean esHash(String valor) {
        return valor != null && valor.matches("pbkdf2\\$\\d+\\$[A-Za-z0-9+/=]+\\$[A-Za-z0-9+/=]+");
    }

    private static byte[] derivar(String contrasena, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), salt, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible en esta JVM", e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Util.Contrasenas <contraseña>");
            return;
        }
        System.out.println(hash(args[0]));
    }
}
